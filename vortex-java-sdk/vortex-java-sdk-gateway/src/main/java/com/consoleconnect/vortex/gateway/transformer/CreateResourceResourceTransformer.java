package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.filter.ResponseBodyTransformerGatewayFilterFactory;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.UserContext;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@AllArgsConstructor
@Service
@Slf4j
public class CreateResourceResourceTransformer extends AbstractResourceTransformer {

  protected final OrderService orderService;

  public byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      ResponseBodyTransformerGatewayFilterFactory.Config config) {
    String orgId = userContext.getOrgId();
    String response = new String(responseBody, StandardCharsets.UTF_8);
    String orderId = null;
    String resourceInstanceId = null;
    if (!StringUtils.isBlank(config.getResourceOrderIdPath())) {
      String orderIdPath =
          String.format("%s.%s", config.getResponseBodyPath(), config.getResourceOrderIdPath());
      orderId = JsonPathToolkit.read(response, orderIdPath);
    }
    if (!StringUtils.isBlank(config.getResourceInstanceIdPath())) {
      String resourceInstanceIdPath =
          String.format("%s.%s", config.getResponseBodyPath(), config.getResourceInstanceIdPath());
      resourceInstanceId = JsonPathToolkit.read(response, resourceInstanceIdPath);
    }
    orderService.createOrder(orgId, orderId, config.getResourceType(), resourceInstanceId);
    return responseBody;
  }

  @Override
  public String getTransformerId() {
    return "resource.create";
  }
}
