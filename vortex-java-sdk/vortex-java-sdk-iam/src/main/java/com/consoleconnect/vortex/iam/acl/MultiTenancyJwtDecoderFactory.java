package com.consoleconnect.vortex.iam.acl;

import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Slf4j
public class MultiTenancyJwtDecoderFactory
    implements JwtDecoderFactory<ResourceServerProperty.TrustedIssuer> {
  @Override
  public JwtDecoder createDecoder(ResourceServerProperty.TrustedIssuer context) {
    if (context.getSecret() != null && !context.getSecret().isEmpty()) {
      log.warn("Using symmetric key for JWT decoding, this is not recommended for production");
      return NimbusJwtDecoder.withSecretKey(
              new SecretKeySpec(context.getSecret().getBytes(), "HMACSHA256"))
          .build();
    }
    return NimbusJwtDecoder.withIssuerLocation(context.getIssuer()).build();
  }
}
