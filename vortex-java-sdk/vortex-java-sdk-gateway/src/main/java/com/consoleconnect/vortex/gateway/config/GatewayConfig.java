package com.consoleconnect.vortex.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
  @Bean
  @ConfigurationProperties(prefix = "app")
  public RouteAdapterConfig adapters() {
    return new RouteAdapterConfig();
  }
}
