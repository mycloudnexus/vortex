package com.consoleconnect.vortex.gateway.adapter;

import com.consoleconnect.vortex.gateway.config.ApiProperty;
import com.consoleconnect.vortex.gateway.service.OrderService;
import lombok.Data;

@Data
public class RouteAdapterContext {
  private ApiProperty apiProperty;
  private OrderService orderService;

  public RouteAdapterContext(ApiProperty apiProperty, OrderService orderService) {
    this.apiProperty = apiProperty;
    this.orderService = orderService;
  }
}
