package com.consoleconnect.vortex.iam.filter;

import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.UserContext;
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

  private final IamProperty iamProperty;

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

              UserContext userContext = new UserContext();
              userContext.setUserId(jwtAuthenticationToken.getName());
              String orgId =
                  jwtAuthenticationToken
                      .getToken()
                      .getClaimAsString(iamProperty.getJwt().getCustomClaims().getOrgId());
              userContext.setOrgId(orgId);
              userContext.setMgmt(iamProperty.getAuth0().getMgmtOrgId().equalsIgnoreCase(orgId));
              userContext.setCustomerId(
                  exchange
                      .getRequest()
                      .getHeaders()
                      .getFirst(IamConstants.X_VORTEX_CUSTOMER_ORG_ID));

              log.info("user context:{}", userContext);
              exchange.getAttributes().put(IamConstants.X_VORTEX_USER_CONTEXT, userContext);
              return Mono.just(jwtAuthenticationToken);
            })
        .then(chain.filter(exchange));
  }
}
