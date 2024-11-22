package com.consoleconnect.vortex.iam.acl;

import static java.util.stream.Collectors.toSet;

import com.consoleconnect.vortex.iam.entity.UserEntity;
import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import com.consoleconnect.vortex.iam.repo.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

  private final JwtDecoder jwtDecoder;
  private final ResourceServerProperty.TrustedIssuer trustedIssuer;
  private final UserRepository userRepository;

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

    if (trustedIssuer.isMgmt()) {
      // Extract userId from the subject, which is in the format of "auth0|<userId>"
      String userId = jwt.getSubject().substring(jwt.getSubject().lastIndexOf("|") + 1);
      log.info("userId:{}", userId);

      Optional<UserEntity> userEntity = userRepository.findOneByUserId(userId);
      if (userEntity.isEmpty() || userEntity.get().getStatus() != UserStatusEnum.ACTIVE) {
        log.error("User is not allowed to access this service {}", userId);
        authentication.setAuthenticated(false);
        return Mono.just(authentication);
      }
      if (resourceRoles == null || resourceRoles.isEmpty()) {
        resourceRoles = userEntity.get().getRoles();
      }
    }

    if (resourceRoles == null || resourceRoles.isEmpty()) {
      resourceRoles = trustedIssuer.getDefaultRoles();
    }

    Collection<GrantedAuthority> grantedAuthorities =
        resourceRoles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
            .collect(toSet());

    log.info("grantedAuthorities:{}", grantedAuthorities);
    return Mono.just(new JwtAuthenticationToken(jwt, grantedAuthorities));
  }
}
