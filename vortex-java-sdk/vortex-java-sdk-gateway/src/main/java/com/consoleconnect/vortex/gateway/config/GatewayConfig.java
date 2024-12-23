package com.consoleconnect.vortex.gateway.config;

import com.consoleconnect.vortex.gateway.model.GatewayProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Bean
  @ConfigurationProperties(prefix = "app.gateway")
  public GatewayProperty gatewayProperty() {
    return new GatewayProperty();
  }
}
