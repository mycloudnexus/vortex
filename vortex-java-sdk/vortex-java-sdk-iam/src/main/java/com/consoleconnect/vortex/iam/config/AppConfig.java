package com.consoleconnect.vortex.iam.config;

import com.consoleconnect.vortex.iam.model.AppProperty;
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
