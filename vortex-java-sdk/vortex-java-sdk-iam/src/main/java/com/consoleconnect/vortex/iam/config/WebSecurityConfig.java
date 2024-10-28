package com.consoleconnect.vortex.iam.config;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import com.consoleconnect.vortex.iam.acl.VortexPermissionEvaluator;
import com.consoleconnect.vortex.iam.filter.UserContextWebFilter;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

  @Bean
  @Order(1)
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      ResourceServerProperty resourceServer,
      VortexPermissionEvaluator permissionEvaluator,
      DefaultMethodSecurityExpressionHandler defaultWebSecurityExpressionHandler,
      IamProperty iamProperty) {
    defaultWebSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator);

    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(
            cors ->
                cors.configurationSource(
                    corsConfigurationSource(
                        resourceServer.getCorsAllowedHeaders(),
                        resourceServer.getCorsAllowedOrigins(),
                        resourceServer.getCorsAllowedMethods())))
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            exchanges -> {
              // permitAll() is used to allow access to the specified paths
              resourceServer
                  .getDefaultAllowedPaths()
                  .forEach(path -> exchanges.pathMatchers(path).permitAll());
              resourceServer
                  .getAllowedPaths()
                  .forEach(path -> exchanges.pathMatchers(path).permitAll());
              // authenticated() is used to require authentication for the specified paths
              resourceServer
                  .getPathPermissions()
                  .forEach(
                      pathPermission ->
                          pathPermission
                              .getHttpMethods()
                              .forEach(
                                  httpMethod ->
                                      exchanges
                                          .pathMatchers(httpMethod, pathPermission.getPath())
                                          .hasAnyRole(
                                              pathPermission.getRoles().toArray(String[]::new))));

              // anyExchange() is used to require authentication for all other paths
              exchanges.anyExchange().authenticated();
            })
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(iamProperty))))
        .addFilterAfter(
            new UserContextWebFilter(iamProperty), SecurityWebFiltersOrder.AUTHORIZATION)
        .build();
  }

  public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(
      IamProperty iamProperty) {

    return new Converter<Jwt, Mono<AbstractAuthenticationToken>>() {
      @Override
      public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.just(new JwtAuthenticationToken(source, extractResourceRoles(source)));
      }

      private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        List<String> resourceRoles =
            jwt.getClaimAsStringList(iamProperty.getJwt().getCustomClaims().getRoles());
        return resourceRoles == null || resourceRoles.isEmpty()
            ? emptySet()
            : resourceRoles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(toSet());
      }
    };
  }

  CorsConfigurationSource corsConfigurationSource(
      List<String> allowedHeaders, List<String> allowedOrigins, List<String> allowedMethods) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(allowedHeaders);
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(allowedMethods);
    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource =
        new UrlBasedCorsConfigurationSource();
    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
    return urlBasedCorsConfigurationSource;
  }
}
