package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultCreateResourceOrderTransformer extends AbstractResourceTransformer<Object> {

  protected final OrderService orderService;

  public DefaultCreateResourceOrderTransformer(OrderService orderService) {
    super(Object.class);
    this.orderService = orderService;
  }

  @Override
  public String doTransform(String responseBody, TransformerContext<Object> context) {

    String orderId = null;

    String resourceInstanceId =
        JsonPathToolkit.read(
            responseBody, "$." + context.getSpecification().getResourceInstanceId());

    log.info("create vortex resource order, api property:{}", context.getSpecification());

    // order
    ResourceTypeEnum resourceTypeEnum =
        ResourceTypeEnum.valueOf(context.getSpecification().getResourceType());
    if (resourceTypeEnum == ResourceTypeEnum.PORT) {
      orderId = resourceInstanceId;
      resourceInstanceId = null;
    }
    orderService.createOrder(
        context.getCustomerId(), orderId, resourceTypeEnum, resourceInstanceId);
    return responseBody;
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.DEFAULT_RESOURCE_CREATE;
  }
}
