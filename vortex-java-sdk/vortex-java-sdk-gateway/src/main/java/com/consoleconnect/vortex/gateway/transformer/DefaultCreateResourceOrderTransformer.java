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
public class DefaultCreateResourceOrderTransformer extends AbstractResourceTransformer<Object> {

  protected final OrderService orderService;

  public DefaultCreateResourceOrderTransformer(OrderService orderService) {
    super(Object.class);
    this.orderService = orderService;
  }

  @Override
  public String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      UserContext userContext,
      TransformerApiProperty config,
      Object metadata) {

    String orderId = null;

    String resourceInstanceId =
        JsonPathToolkit.read(responseBody, "$." + config.getResourceInstanceId());

    log.info("create vortex resource order, api property:{}", config);

    // order
    if (config.getResourceType() == ResourceTypeEnum.PORT) {
      orderId = resourceInstanceId;
      resourceInstanceId = null;
    }
    orderService.createOrder(customerId, orderId, config.getResourceType(), resourceInstanceId);
    return responseBody;
  }

  @Override
  public String getTransformerId() {
    return "resource.create.legacy";
  }
}
