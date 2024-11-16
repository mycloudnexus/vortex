package com.consoleconnect.vortex.iam.acl;

import static java.util.stream.Collectors.toSet;

import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

  private final NimbusJwtDecoder jwtDecoder;
  private final ResourceServerProperty.TrustedIssuer trustedIssuer;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String jwtToken = authentication.getCredentials().toString();

    Jwt jwt = null;
    try {
      jwt = jwtDecoder.decode(jwtToken);
    } catch (Exception e) {
      log.error("Error: {}", e.getMessage());
      authentication.setAuthenticated(false);
      return Mono.just(authentication);
    }

    List<String> resourceRoles =
        jwt.getClaimAsStringList(trustedIssuer.getCustomClaims().getRoles());
    Collection<GrantedAuthority> grantedAuthorities =
        resourceRoles == null || resourceRoles.isEmpty()
            ? trustedIssuer.getDefaultRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(toSet())
            : resourceRoles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(toSet());
    log.info("grantedAuthorities:{}", grantedAuthorities);
    return Mono.just(new JwtAuthenticationToken(jwt, grantedAuthorities));
  }
}
