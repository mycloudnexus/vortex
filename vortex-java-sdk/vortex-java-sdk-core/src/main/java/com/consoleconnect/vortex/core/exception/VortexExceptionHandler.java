package com.consoleconnect.vortex.core.exception;

import com.consoleconnect.vortex.core.toolkit.StringUtils;
import java.util.*;
import lombok.Getter;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Order(Integer.MIN_VALUE)
public class VortexExceptionHandler extends AbstractErrorWebExceptionHandler {
  private static final int REASON_LENGTH_UPPER_LIMIT = 255;
  @Getter List<TriConsumer<ServerRequest, Object, HttpStatusCode>> callbackList = new ArrayList<>();
  private static final String ERR_MSG_FORMAT = "time:%s, error: %s, path:%s";

  public VortexExceptionHandler(
      final ErrorAttributes errorAttributes,
      final WebProperties.Resources resources,
      final ServerCodecConfigurer serverCodecConfigurer,
      final ApplicationContext applicationContext) {
    super(errorAttributes, resources, applicationContext);
    setMessageReaders(serverCodecConfigurer.getReaders());
    setMessageWriters(serverCodecConfigurer.getWriters());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    ErrorAttributeOptions options =
        ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.MESSAGE,
            ErrorAttributeOptions.Include.EXCEPTION,
            ErrorAttributeOptions.Include.BINDING_ERRORS);
    Map<String, Object> map = getErrorAttributes(request, options);
    Throwable throwable = getError(request);
    HttpStatusCode httpStatus = determineHttpStatus(throwable);
    String message =
        String.format(ERR_MSG_FORMAT, map.get("timestamp"), map.get("error"), map.get("path"));

    Object errorBody = generateBody(httpStatus, message, throwable);
    return ServerResponse.status(httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorBody))
        .doOnNext(
            serverResponse ->
                callbackList.forEach(action -> action.accept(request, errorBody, httpStatus)));
  }

  public Object generateBody(HttpStatusCode httpStatus, String message, Throwable throwable) {
    VortexError errorResponse = new VortexError();
    errorResponse.setCode(httpStatus.value());
    // The max length of reason is 255.
    String reasonMsg = throwable.getMessage();

    // Handle controller binding exception.
    if (throwable instanceof WebExchangeBindException) {
      WebExchangeBindException exchangeBindException = (WebExchangeBindException) throwable;
      reasonMsg =
          StringUtils.truncate(
              exchangeBindException.getFieldErrors().get(0).getDefaultMessage(),
              REASON_LENGTH_UPPER_LIMIT);
    }
    String errMsg = StringUtils.truncate(throwable.getMessage(), REASON_LENGTH_UPPER_LIMIT);
    errorResponse.setReason(reasonMsg);

    message =
        message == null
            ? VortexError.ErrorMapping.defaultMsg(httpStatus.value(), throwable.getMessage())
            : message;

    errorResponse.setMessage(
        (Objects.isNull(throwable.getCause()) ? message : throwable.getCause().getMessage()));
    errorResponse.setReferenceError(errMsg);
    if (httpStatus.value() != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      return errorResponse;
    }
    return List.of(errorResponse);
  }

  private HttpStatusCode determineHttpStatus(Throwable throwable) {
    if (throwable instanceof ResponseStatusException responseStatusException) {
      return responseStatusException.getStatusCode();
    } else if (throwable instanceof VortexException vortexException) {
      return HttpStatus.valueOf(vortexException.getCode());
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public void registerCallback(TriConsumer<ServerRequest, Object, HttpStatusCode> consumer) {
    callbackList.add(consumer);
  }
}
