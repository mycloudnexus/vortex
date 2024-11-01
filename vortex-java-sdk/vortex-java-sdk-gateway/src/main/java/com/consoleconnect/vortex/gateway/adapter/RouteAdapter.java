package com.consoleconnect.vortex.gateway.adapter;

import org.springframework.web.server.ServerWebExchange;

public interface RouteAdapter {

  byte[] process(ServerWebExchange exchange, byte[] responseBody);
}
