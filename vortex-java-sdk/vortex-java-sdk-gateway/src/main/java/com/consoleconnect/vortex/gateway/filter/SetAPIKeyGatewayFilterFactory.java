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
      UserContext userContext = exchange.getAttribute(IamConstants.X_VORTEX_USER_CONTEXT);
      if (userContext == null) {
        log.warn("User context is null,SetAPIKeyGatewayFilterFactory will not be applied");
        return chain.filter(exchange);
      } else {
        String apiKeyValue = userContext.isMgmt() ? config.getAdminKey() : config.getUserKey();
        ServerWebExchange updatedExchange =
            exchange
                .mutate()
                .request(
                    request ->
                        request.headers(
                            httpHeaders -> httpHeaders.add(config.getKeyName(), apiKeyValue)))
                .build();

        return chain.filter(updatedExchange);
      }
    });
  }

  @Data
  public static class Config {
    private String keyName;
    private String adminKey;
    private String userKey;
  }
}
