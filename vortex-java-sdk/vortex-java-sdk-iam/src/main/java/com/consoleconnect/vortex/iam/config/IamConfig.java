package com.consoleconnect.vortex.iam.config;

import com.consoleconnect.vortex.cc.CCHttpClient;
import com.consoleconnect.vortex.cc.model.CCClientProperty;
import com.consoleconnect.vortex.core.toolkit.GenericHttpClient;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.ResourceServerProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class IamConfig {

  @Bean
  @ConfigurationProperties(prefix = "app.iam")
  public IamProperty appProperty() {
    return new IamProperty();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.iam.auth0")
  public Auth0Property auth0Property() {
    return new Auth0Property();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.iam.resource-server")
  public ResourceServerProperty resourceServerProperty() {
    return new ResourceServerProperty();
  }

  @Bean
  public Auth0Client auth0Client(Auth0Property auth0Property) {
    return new Auth0Client(auth0Property);
  }

  @Bean
  public CCHttpClient ccHttpClient(IamProperty appProperty, WebClient webClient) {
    CCClientProperty clientProperty = new CCClientProperty();
    clientProperty.setBaseUrl(appProperty.getDownStream().getBaseUrl());
    clientProperty.setAdminApiKey(appProperty.getDownStream().getAdminApiKey());
    clientProperty.setUserApiKey(appProperty.getDownStream().getUserApiKey());
    clientProperty.setApiKeyName(appProperty.getDownStream().getApiKeyName());
    clientProperty.setCompanyUsername(appProperty.getDownStream().getCompanyUsername());
    clientProperty.setCompanyId(appProperty.getDownStream().getCompanyId());

    return new CCHttpClient(clientProperty, new GenericHttpClient(webClient));
  }
}
