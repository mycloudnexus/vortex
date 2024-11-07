package com.consoleconnect.vortex.gateway.adapter;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.service.OrderService;
import lombok.Data;

@Data
public class RouteAdapterContext {
  private ResourceTypeEnum resourceType;
  private OrderService orderService;

  public RouteAdapterContext(ResourceTypeEnum resourceType, OrderService orderService) {
    this.resourceType = resourceType;
    this.orderService = orderService;
  }
}
