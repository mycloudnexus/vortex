package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
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
      UserContext userContext = exchange.getAttribute(IamConstants.X_USER_CONTEXT);
      if (userContext == null || userContext.getAccessToken() == null) {
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
                                httpHeaders.add(
                                    config.getAccessTokenHeaderName(),
                                    userContext.getAccessToken())))
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
