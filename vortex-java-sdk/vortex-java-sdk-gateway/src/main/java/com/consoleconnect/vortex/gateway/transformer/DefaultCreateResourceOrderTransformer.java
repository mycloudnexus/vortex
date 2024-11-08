package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.UserContext;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultCreateResourceOrderTransformer extends AbstractResourceTransformer {

  protected final OrderService orderService;

  @Override
  public byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      TransformerApiProperty config) {

    String orgId = userContext.getCustomerId();

    String response = new String(responseBody, StandardCharsets.UTF_8);

    String orderId = null;

    String resourceInstanceId =
        JsonPathToolkit.read(response, "$." + config.getResourceInstanceId());

    log.info("create vortex resource order, api property:{}", config);

    // order
    if (config.getResourceType() == ResourceTypeEnum.PORT) {
      orderId = resourceInstanceId;
      resourceInstanceId = null;
    }

    orderService.createOrder(orgId, orderId, config.getResourceType(), resourceInstanceId);
    return responseBody;
  }

  @Override
  public String getTransformerId() {
    return "resource.create";
  }
}
