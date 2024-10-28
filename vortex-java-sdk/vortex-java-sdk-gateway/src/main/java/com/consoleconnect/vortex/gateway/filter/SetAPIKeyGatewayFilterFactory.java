package com.consoleconnect.vortex.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class SetAPIKeyGatewayFilterFactory
    extends AbstractGatewayFilterFactory<SetAPIKeyGatewayFilterFactory.Config> {

  public SetAPIKeyGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    // ...
    return ((exchange, chain) -> {
      String role = exchange.getAttribute("x-vortex-user-role");

      String apiKeyValue =
          "admin".equalsIgnoreCase(role) ? config.getAdminKey() : config.getUserKey();

      ServerWebExchange updatedExchange =
          exchange
              .mutate()
              .request(
                  request ->
                      request.headers(
                          httpHeaders -> httpHeaders.add(config.getKeyName(), apiKeyValue)))
              .build();

      return chain.filter(updatedExchange);
    });
  }

  @Data
  public static class Config {
    private String keyName;
    private String adminKey;
    private String userKey;
  }
}
