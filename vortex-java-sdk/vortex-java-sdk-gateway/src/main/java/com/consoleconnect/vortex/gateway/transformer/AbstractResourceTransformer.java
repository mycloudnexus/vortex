package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractResourceTransformer {

  public final byte[] transform(
      ServerWebExchange exchange, byte[] responseBody, TransformerApiProperty config) {
    long start = System.currentTimeMillis();
    try {
      UserContext userContext = exchange.getAttribute(IamConstants.X_USER_CONTEXT);
      return doTransform(exchange, responseBody, userContext, config);
    } catch (Exception e) {
      log.error("{} transform error.", getClass().getSimpleName(), e);
      throw VortexException.badRequest("Failed to transform", e);
    } finally {
      log.info(
          "{} transform cost: {} ms.",
          getClass().getSimpleName(),
          System.currentTimeMillis() - start);
    }
  }

  protected abstract byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      TransformerApiProperty config);

  public abstract String getTransformerId();
}
