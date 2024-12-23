package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import com.consoleconnect.vortex.iam.model.UserContext;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  public UserContext createUserContext(JwtAuthenticationToken jwtAuthenticationToken) {
    UserContext userContext = new UserContext();
    userContext.setUserId(jwtAuthenticationToken.getName());

    String issuer = jwtAuthenticationToken.getToken().getIssuer().toString();

    Optional<ResourceServerProperty.TrustedIssuer> trustedIssuerOptional =
        iamProperty.getResourceServer().getTrustedIssuers().stream()
            .filter(trustedIssuer -> issuer.contains(trustedIssuer.getIssuer()))
            .findFirst();
    if (trustedIssuerOptional.isEmpty()) {
      String errorMsg =
          String.format(
              "No trusted issuer found for issuer:%s",
              jwtAuthenticationToken.getToken().getIssuer().toString());
      throw VortexException.badRequest(errorMsg);
    }
    ResourceServerProperty.TrustedIssuer trustedIssuer = trustedIssuerOptional.get();
    userContext.setUserType(
        trustedIssuer.isMgmt() ? UserTypeEnum.MGMT_USER : UserTypeEnum.CUSTOMER_USER);
    String orgId =
        jwtAuthenticationToken
            .getToken()
            .getClaimAsString(trustedIssuer.getCustomClaims().getOrgId());
    if (orgId == null) {
      orgId = trustedIssuer.getDefaultOrgId();
    }
    if (StringUtils.isBlank(orgId)) {
      log.error("orgId is null for user:{}", userContext.getUserId());
      throw VortexException.badRequest("orgId is null");
    }
    userContext.setOrgId(orgId);

    if (trustedIssuer.getUserIdPrefix() != null) {
      userContext.setUserId(userContext.getUserId().replace(trustedIssuer.getUserIdPrefix(), ""));
    }

    userContext.setTrustedIssuer(trustedIssuer);
    userContext.setApiServer(iamProperty.getDownStream().getBaseUrl());
    String tokenValue =
        trustedIssuer.isMgmt()
            ? jwtAuthenticationToken.getToken().getTokenValue()
            : iamProperty.getDownStream().getToken();
    userContext.setAccessToken(iamProperty.getDownStream().getTokenPrefix() + tokenValue);

    userContext.setRoles(
        jwtAuthenticationToken.getAuthorities().stream().map(Object::toString).toList());

    return userContext;
  }

  public Mono<String> getOrgId() {
    return getAuthentication()
        .map(this::createUserContext)
        .map(UserContext::getOrgId)
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
