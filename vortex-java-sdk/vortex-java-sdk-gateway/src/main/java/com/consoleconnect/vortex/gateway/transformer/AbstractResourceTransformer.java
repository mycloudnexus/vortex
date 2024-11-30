package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.IamConstants;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractResourceTransformer<T> {

  public static final String VAR_RESOURCES = "resources";
  public static final String VAR_ORDER_IDS = "orderIds";
  public static final String VAR_RESOURCE_IDS = "resourceIds";
  public static final String VAR_DATA = "data";
  public static final String VAR_CUSTOMER_ID = "customerId";
  public static final String VAR_CUSTOMER_NAME = "customerName";
  public static final String VAR_IS_MGMT = "isMgmt";

  private final Class<T> cls;

  protected AbstractResourceTransformer(Class<T> cls) {
    this.cls = cls;
  }

  public final byte[] transform(
      ServerWebExchange exchange,
      byte[] responseBody,
      TransformerSpecification.Default specification) {
    log.info("Start to transform,specification:{}", specification);
    long start = System.currentTimeMillis();
    try {
      String customerId = exchange.getAttribute(IamConstants.X_VORTEX_CUSTOMER_ID);
      Boolean isMgt = exchange.getAttribute(IamConstants.X_VORTEX_MGMT_ORG);

      TransformerSpecification<T> specificationInternal = specification.copy(cls);
      String responseBodyJsonStr = new String(responseBody, StandardCharsets.UTF_8);
      TransformerContext<T> context = new TransformerContext<>();
      context.setCustomerId(customerId);
      context.setSpecification(specificationInternal);
      context.setMgmt(isMgt != null && isMgt);

      return doTransform(responseBodyJsonStr, context).getBytes(StandardCharsets.UTF_8);
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

  protected abstract String doTransform(String data, TransformerContext<T> context);

  public abstract TransformerIdentityEnum getTransformerId();

  protected String extraData(String responseBody, String responseDataPath) {
    return TransformerSpecification.JSON_ROOT.equalsIgnoreCase(responseDataPath)
        ? responseBody
        : JsonToolkit.toJson(JsonPathToolkit.read(responseBody, responseDataPath, Object.class));
  }
}
