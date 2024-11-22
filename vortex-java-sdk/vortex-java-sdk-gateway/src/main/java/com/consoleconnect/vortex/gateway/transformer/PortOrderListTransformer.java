package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
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
import org.springframework.web.server.ServerWebExchange;

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
  public String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      String customerId,
      TransformerApiProperty config,
      Object metadata) {

    List<OrderEntity> fillOrders = new ArrayList<>();
    Map<String, OrderEntity> orders =
        orderService.listOrders(customerId, config.getResourceType()).stream()
            .collect(Collectors.toMap(OrderEntity::getOrderId, x -> x));

    Set<String> orderIds = orders.keySet();

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> resOrders = ctx.read(config.getResponseBodyPath());
    // filter the orders of the reseller/customer organization
    resOrders.removeIf(
        o -> {
          String orderId = (String) o.get(config.getResourceInstanceId());
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

    if (TransformerApiProperty.DEFAULT_BODY_PATH.equals(config.getResponseBodyPath())) {
      // override ctx
      ctx = JsonPathToolkit.createDocCtx(resOrders);
    } else {
      // ctx.set("$.results", resOrders)
      ctx.set(config.getResponseBodyPath(), resOrders);
    }
    orderService.fillOrdersPortId(fillOrders);

    return ctx.jsonString();
  }

  @Override
  public String getTransformerId() {
    return "port.order.list";
  }
}
