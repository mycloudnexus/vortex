package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class Auth0Property {
  private String domain;
  private String clientId;
  private String clientSecret;
  private String audience;
}
