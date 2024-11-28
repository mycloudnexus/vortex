package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.iam.model.IamConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class SetAPIKeyGatewayFilterFactory
    extends AbstractGatewayFilterFactory<SetAPIKeyGatewayFilterFactory.Config> {

  public SetAPIKeyGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    // ...
    return ((exchange, chain) -> {
      String accessToken = exchange.getAttribute(IamConstants.X_VORTEX_ACCESS_TOKEN);
      if (accessToken == null) {
        log.warn("AccessToken is null,SetAPIKeyGatewayFilterFactory will not be applied");
        return chain.filter(exchange);
      } else {
        ServerWebExchange updatedExchange =
            exchange
                .mutate()
                .request(
                    request ->
                        request.headers(
                            httpHeaders ->
                                httpHeaders.add(config.getAccessTokenHeaderName(), accessToken)))
                .build();

        return chain.filter(updatedExchange);
      }
    });
  }

  @Data
  public static class Config {
    private String accessTokenHeaderName = "Authorization";
  }
}
