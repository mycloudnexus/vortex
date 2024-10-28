package com.consoleconnect.vortex.iam.model;

import java.util.List;
import lombok.Data;

@Data
public class Auth0Property {

  private Config mgmtApi;
  private Config app;

  private List<Role> roles;
  private String mgmtOrgId;

  @Data
  public static class Config {
    private String domain;
    private String clientId;
    private String clientSecret;
    private String audience;
  }

  @Data
  public static class Role {
    private String roleId;
    private List<String> orgIds;
  }
}
