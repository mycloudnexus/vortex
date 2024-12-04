package com.consoleconnect.vortex.core.config;

import com.consoleconnect.vortex.core.model.AppProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  @ConfigurationProperties(prefix = "app")
  public AppProperty appProperty() {
    return new AppProperty();
  }
}
