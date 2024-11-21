package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class DownstreamProperty {
  private String baseUrl;
  private String apiKeyName;
  private String adminApiKey;
  private String userApiKey;
  private String role;
  private String companyUsername;
  private String companyId;
}
