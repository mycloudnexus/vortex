package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class DownstreamProperty {
  private String baseUrl;
  private String apiKeyName;
  private String adminApiKey;
  private String userApiKey;
  private String role;
  private String roleEndpoint;
  private String companyUsername;
  private String companyId;
  private String membersEndpoint;
  private String userInfoEndpoint;
  private String userAuthEndpoint;
}
