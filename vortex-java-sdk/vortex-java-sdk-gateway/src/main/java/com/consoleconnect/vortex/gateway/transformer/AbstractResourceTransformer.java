package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import com.consoleconnect.vortex.iam.model.IamConstants;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
  public static final String VAR_USER_TYPE = "userType";
  public static final String VAR_USER_ID = "userId";

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
      UserTypeEnum userType = exchange.getAttribute(IamConstants.X_VORTEX_USER_TYPE);

      TransformerSpecification<T> specificationInternal = specification.copy(cls);
      String responseBodyJsonStr = new String(responseBody, StandardCharsets.UTF_8);
      TransformerContext<T> context = new TransformerContext<>();
      context.setCustomerId(customerId);
      context.setSpecification(specificationInternal);
      context.setLoginUserType(userType);

      byte[] result = responseBody;
      if (canTransform(context)) {
        result = doTransform(responseBodyJsonStr, context).getBytes(StandardCharsets.UTF_8);
      } else {
        log.info("Skip transform,condition not met.");
      }
      log.info("Transform done, time: {} ms.", System.currentTimeMillis() - start);
      return result;
    } catch (Exception e) {
      String errorMsg = "Failed to transform,error:{}" + e.getMessage();
      log.error("{}", errorMsg);
      throw VortexException.badRequest(errorMsg);
    }
  }

  protected abstract String doTransform(String data, TransformerContext<T> context);

  public abstract TransformerIdentityEnum getTransformerId();

  protected String extraData(String responseBody, String responseDataPath) {
    return TransformerSpecification.JSON_ROOT.equalsIgnoreCase(responseDataPath)
        ? responseBody
        : JsonToolkit.toJson(JsonPathToolkit.read(responseBody, responseDataPath, Object.class));
  }

  public boolean canTransform(TransformerContext<T> context) {
    log.info("Check if can transform,context:{}", context.getLoginUserType());
    if (context.getSpecification().getWhen() == null
        || context.getSpecification().getWhen().isEmpty()) {
      log.info("No condition,transform directly.");
      return true;
    }

    Map<String, Object> variables = new HashMap<>();
    variables.put(VAR_CUSTOMER_ID, context.getCustomerId());
    variables.put(VAR_USER_TYPE, context.getLoginUserType().name());

    log.info("Variables:{}", variables);
    log.info("when:{}", context.getSpecification().getWhen());
    Boolean conditionOn =
        SpelExpressionEngine.evaluate(
            context.getSpecification().getWhen(), variables, Boolean.class);
    log.info("Condition on:{}", conditionOn);
    return conditionOn != null && conditionOn;
  }
}
