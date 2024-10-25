package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class AppProperty {
  private Auth0Property auth0;
  private ResourceServerProperty resourceServer;
}
