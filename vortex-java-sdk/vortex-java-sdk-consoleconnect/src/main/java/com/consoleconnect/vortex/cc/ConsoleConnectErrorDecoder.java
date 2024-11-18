package com.consoleconnect.vortex.cc;

import com.consoleconnect.vortex.cc.model.ErrorResponse;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleConnectErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {

    if (response.body() != null) {
      try {
        String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        log.error("{}", body);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
        if (errorResponse.getError() != null)
          return new VortexException(
              errorResponse.getError().getStatusCode(), errorResponse.getError().toString());
      } catch (Exception e) {
        log.warn("{}", e.getMessage());
      }
    }
    return new VortexException(response.status(), response.toString());
  }
}
