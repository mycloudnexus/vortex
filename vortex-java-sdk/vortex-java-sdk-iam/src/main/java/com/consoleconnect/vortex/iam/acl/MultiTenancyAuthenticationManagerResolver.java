package com.consoleconnect.vortex.iam.acl;

import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import com.consoleconnect.vortex.iam.repo.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class MultiTenancyAuthenticationManagerResolver
    implements ReactiveAuthenticationManagerResolver<String> {

  private final IamProperty iamProperty;
  private final UserRepository userRepository;
  private final MultiTenancyJwtDecoderFactory jwtDecoderFactory;

  @Override
  public Mono<ReactiveAuthenticationManager> resolve(String issuer) {
    log.info("finding authentication manager via issuer:{}", issuer);
    Optional<ResourceServerProperty.TrustedIssuer> trustedTokenIssuer =
        iamProperty.getResourceServer().getTrustedIssuers().stream()
            .filter(tokenIssuer -> tokenIssuer.getIssuer().equalsIgnoreCase(issuer))
            .findFirst();

    if (trustedTokenIssuer.isEmpty()) {
      log.warn("no trusted token issuer found for issuer:{}", issuer);
      return Mono.empty();
    }

    log.info("found trusted token issuer for issuer:{}", issuer);

    return Mono.just(
        new JwtAuthenticationManager(
            jwtDecoderFactory.createDecoder(trustedTokenIssuer.get()),
            trustedTokenIssuer.get(),
            userRepository));
  }
}
