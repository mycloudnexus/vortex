package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.jayway.jsonpath.DocumentContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PortOrderListTransformer extends AbstractResourceTransformer<Object> {

  protected final OrderService orderService;

  public PortOrderListTransformer(OrderService orderService) {
    super(Object.class);
    this.orderService = orderService;
  }

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public String doTransform(String responseBody, TransformerContext<Object> context) {

    List<OrderEntity> fillOrders = new ArrayList<>();
    Map<String, OrderEntity> orders =
        orderService
            .listOrders(context.getCustomerId(), context.getSpecification().getResourceType())
            .stream()
            .collect(Collectors.toMap(OrderEntity::getOrderId, x -> x));

    Set<String> orderIds = orders.keySet();

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> resOrders =
        ctx.read(context.getSpecification().getResponseDataPath());
    // filter the orders of the reseller/customer organization
    resOrders.removeIf(
        o -> {
          String orderId = (String) o.get(context.getSpecification().getResourceInstanceId());
          if (orderIds.contains(orderId)) {
            // try filling the port id when list port orders
            OrderEntity order = orders.get(orderId);
            String resourceId = (String) o.get("createdPortId");
            if (order.getResourceId() == null && resourceId != null) {
              order.setResourceType(ResourceTypeEnum.PORT);
              order.setResourceId(resourceId);
              fillOrders.add(order);
            }
            return Boolean.FALSE;
          }
          return Boolean.TRUE;
        });

    if (TransformerSpecification.JSON_ROOT.equals(
        context.getSpecification().getResponseDataPath())) {
      // override ctx
      ctx = JsonPathToolkit.createDocCtx(resOrders);
    } else {
      // ctx.set("$.results", resOrders)
      ctx.set(context.getSpecification().getResponseDataPath(), resOrders);
    }
    orderService.fillOrdersPortId(fillOrders);

    return ctx.jsonString();
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.PORT_ORDER_LIST;
  }
}
