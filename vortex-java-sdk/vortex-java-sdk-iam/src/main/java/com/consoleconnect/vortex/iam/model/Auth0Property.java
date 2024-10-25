package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class Auth0Property {

  private Config mgmtApi;
  private Config app;

  private String mgmtOrgId; // the organizationId for mgmt users
  private Roles roles; // the role ids for mgmt, organization admin and organization user

  @Data
  public static class Config {
    private String domain;
    private String clientId;
    private String clientSecret;
    private String audience;
  }

  @Data
  public static class Roles {
    private String mgmtRoleId; // the role id for platform mgmt only
    private String adminRoleId; // the role id for organization admin
    private String userRoleId; // the role id for organization user
  }
}
