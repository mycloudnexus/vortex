package com.consoleconnect.vortex.iam.config;

import com.consoleconnect.vortex.iam.acl.MultiTenancyAuthenticationManagerResolver;
import com.consoleconnect.vortex.iam.filter.UserContextWebFilter;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import com.consoleconnect.vortex.iam.repo.UserRepository;
import com.consoleconnect.vortex.iam.service.UserContextService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
      UserContextService userContextService,
      UserRepository userRepository,
      IamProperty iamProperty) {

    ServerHttpSecurity serverHttpSecurity =
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
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
                                                  pathPermission
                                                      .getRoles()
                                                      .toArray(String[]::new))));

                  // anyExchange() is used to require authentication for all other paths
                  exchanges.anyExchange().authenticated();
                });

    serverHttpSecurity =
        serverHttpSecurity.oauth2ResourceServer(
            oauth2 ->
                oauth2.authenticationManagerResolver(
                    new JwtIssuerReactiveAuthenticationManagerResolver(
                        new MultiTenancyAuthenticationManagerResolver(
                            iamProperty, userRepository))));

    return serverHttpSecurity
        .addFilterAfter(
            new UserContextWebFilter(userContextService), SecurityWebFiltersOrder.AUTHORIZATION)
        .build();
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
