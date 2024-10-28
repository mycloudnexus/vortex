package com.consoleconnect.vortex.iam.filter;

import com.consoleconnect.vortex.iam.model.IamProperty;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class JWTSecurityGlobalFilter implements WebFilter, Ordered {

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
              String orgId =
                  jwtAuthenticationToken
                      .getToken()
                      .getClaimAsString(iamProperty.getJwt().getCustomClaims().getOrgId());
              String userRole =
                  orgId.equalsIgnoreCase(iamProperty.getAuth0().getMgmtOrgId()) ? "admin" : "user";
              exchange.getAttributes().put("x-vortex-user-role", userRole);
              exchange.getAttributes().put("x-vortex-org-id", orgId);
              return Mono.just(jwtAuthenticationToken);
            })
        .then(chain.filter(exchange));
  }
}
