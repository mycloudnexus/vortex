package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class Auth0Config {
  private String domain;
  private String spaClientId;
  private String clientId;
  private String clientSecret;
}
