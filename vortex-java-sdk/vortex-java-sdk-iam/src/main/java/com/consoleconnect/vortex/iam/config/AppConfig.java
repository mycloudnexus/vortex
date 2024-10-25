package com.consoleconnect.vortex.iam.config;

import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.model.AppProperty;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
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

  @Bean
  @ConfigurationProperties(prefix = "app.auth0")
  public Auth0Property auth0Property() {
    return new Auth0Property();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.resource-server")
  public ResourceServerProperty resourceServerProperty() {
    return new ResourceServerProperty();
  }

  @Bean
  public Auth0Client auth0Client(Auth0Property auth0Property) {
    return new Auth0Client(auth0Property);
  }
}
