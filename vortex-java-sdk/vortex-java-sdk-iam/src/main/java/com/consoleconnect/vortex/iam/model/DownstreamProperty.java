package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class DownstreamProperty {
  private String baseUrl;
  private String adminApiKeyName;
  private String adminApiKey;
  private String role;
  private String roleEndpoint;
  private String companyName;
}
