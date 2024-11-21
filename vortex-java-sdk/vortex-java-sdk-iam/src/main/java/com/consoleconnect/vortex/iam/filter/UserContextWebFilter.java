package com.consoleconnect.vortex.iam.filter;

import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.iam.service.UserContextService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class UserContextWebFilter implements WebFilter, Ordered {

  private final UserContextService userContextService;

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(
            securityContext -> {
              JwtAuthenticationToken jwtAuthenticationToken =
                  (JwtAuthenticationToken) securityContext.getAuthentication();

              UserContext userContext =
                  userContextService.createUserContext(jwtAuthenticationToken);

              String customerId = userContext.getOrgId();
              if (userContext.isMgmt()
                  && exchange
                      .getRequest()
                      .getHeaders()
                      .containsKey(IamConstants.X_VORTEX_CUSTOMER_ID)) {
                // customerId can be customized by the client in the header
                customerId =
                    exchange.getRequest().getHeaders().getFirst(IamConstants.X_VORTEX_CUSTOMER_ID);
              }
              userContext.setCustomerId(customerId);
              log.info("user context:{}", userContext);
              exchange.getAttributes().put(IamConstants.X_VORTEX_USER_ID, userContext.getUserId());
              exchange
                  .getAttributes()
                  .put(IamConstants.X_VORTEX_USER_ORG_ID, userContext.getOrgId());
              exchange
                  .getAttributes()
                  .put(IamConstants.X_VORTEX_CUSTOMER_ID, userContext.getCustomerId());
              exchange
                  .getAttributes()
                  .put(IamConstants.X_VORTEX_ACCESS_TOKEN, userContext.getAccessToken());
              return Mono.just(jwtAuthenticationToken);
            })
        .then(chain.filter(exchange));
  }
}
