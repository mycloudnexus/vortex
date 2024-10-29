package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.iam.model.IamProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
@Slf4j
public class UserContextService {
  private final IamProperty iamProperty;
  public static final String ANONYMOUS = "anonymous";

  public Mono<String> getOrgId() {
    return getAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(JwtClaimAccessor::getClaims)
        .map(claim -> claim.get(iamProperty.getJwt().getCustomClaims().getOrgId()))
        .map(Object::toString)
        .switchIfEmpty(Mono.just(ANONYMOUS));
  }

  public static Mono<String> getUserId() {
    return getAuthentication()
        .map(JwtAuthenticationToken::getToken)
        .map(JwtClaimAccessor::getSubject)
        .switchIfEmpty(Mono.just(ANONYMOUS));
  }

  public static Mono<JwtAuthenticationToken> getAuthentication() {
    try {
      return ReactiveSecurityContextHolder.getContext()
          .map(SecurityContext::getAuthentication)
          .cast(JwtAuthenticationToken.class);
    } catch (Exception ex) {
      log.warn("Failed to get authentication", ex);
      return Mono.empty();
    }
  }
}
