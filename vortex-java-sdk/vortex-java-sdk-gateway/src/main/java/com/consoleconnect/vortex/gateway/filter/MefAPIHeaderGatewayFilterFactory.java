package com.consoleconnect.vortex.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class MefAPIHeaderGatewayFilterFactory
    extends AbstractGatewayFilterFactory<MefAPIHeaderGatewayFilterFactory.Config> {

  public MefAPIHeaderGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return ((exchange, chain) -> {
      ServerWebExchange updatedExchange =
          exchange
              .mutate()
              .request(
                  request ->
                      request.headers(
                          httpHeaders -> {
                            if (exchange
                                .getRequest()
                                .getURI()
                                .getPath()
                                .startsWith(config.getPrefix())) {
                              httpHeaders.add(config.getKey(), config.getKeyValue());
                            }
                          }))
              .build();

      return chain.filter(updatedExchange);
    });
  }

  @Data
  public static class Config {
    private String prefix;
    private String key;
    private String keyValue;
  }
}
