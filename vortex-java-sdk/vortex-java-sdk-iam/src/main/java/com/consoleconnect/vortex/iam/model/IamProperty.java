package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class IamProperty {
  private Auth0Property auth0;
  private ResourceServerProperty resourceServer;
  private JwtProperty jwt;

  @Data
  public static class JwtProperty {
    private String issuerUri;
    private String jwkSetUri;
    private String audiences;

    private CustomClaims customClaims = new CustomClaims();
  }

  @Data
  public static class CustomClaims {
    private String permissions = "permissions";
    private String roles = "org_roles";
    private String orgId = "org_id";
  }
}
