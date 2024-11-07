package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.filter.ResponseBodyTransformerGatewayFilterFactory;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractResourceTransformer {

  public final byte[] transform(
      ServerWebExchange exchange,
      byte[] responseBody,
      ResponseBodyTransformerGatewayFilterFactory.Config config) {
    long start = System.currentTimeMillis();
    try {
      UserContext userContext = exchange.getAttribute(IamConstants.X_VORTEX_USER_CONTEXT);
      return doTransform(exchange, responseBody, userContext, config);
    } catch (Exception e) {
      log.error("{} process error.", getClass().getSimpleName(), e);
      throw VortexException.badRequest("Failed to process", e);
    } finally {
      log.info(
          "{} process cost: {} ms.",
          getClass().getSimpleName(),
          System.currentTimeMillis() - start);
    }
  }

  protected abstract byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      ResponseBodyTransformerGatewayFilterFactory.Config config);

  public abstract String getTransformerId();
}
