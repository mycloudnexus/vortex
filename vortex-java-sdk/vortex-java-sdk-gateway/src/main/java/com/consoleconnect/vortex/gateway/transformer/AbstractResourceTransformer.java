package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.IamConstants;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractResourceTransformer<T> {

  private final Class<T> cls;

  protected AbstractResourceTransformer(Class<T> cls) {
    this.cls = cls;
  }

  public final byte[] transform(
      ServerWebExchange exchange, byte[] responseBody, TransformerApiProperty config) {
    long start = System.currentTimeMillis();
    try {
      String customerId = exchange.getAttribute(IamConstants.X_VORTEX_CUSTOMER_ID);
      T metadata = JsonToolkit.fromJson(JsonToolkit.toJson(config.getMetadata()), cls);
      String responseBodyJsonStr = new String(responseBody, StandardCharsets.UTF_8);
      return doTransform(exchange, responseBodyJsonStr, customerId, config, metadata)
          .getBytes(StandardCharsets.UTF_8);
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

  protected abstract String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      String customerId,
      TransformerApiProperty config,
      T metadata);

  public String readJsonPath(String responseBody, String jsonPath, TransformerApiProperty config) {
    String fullPath = String.format("%s.%s", config.getResponseBodyPath(), jsonPath);
    return JsonPathToolkit.read(responseBody, fullPath);
  }

  public abstract String getTransformerId();
}
