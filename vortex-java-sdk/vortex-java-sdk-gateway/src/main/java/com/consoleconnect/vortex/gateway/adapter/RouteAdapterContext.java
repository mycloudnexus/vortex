package com.consoleconnect.vortex.gateway.adapter;

import com.consoleconnect.vortex.gateway.service.OrderService;
import lombok.Data;

@Data
public class RouteAdapterContext {
  private OrderService orderService;

  public RouteAdapterContext(OrderService orderService) {
    this.orderService = orderService;
  }
}
