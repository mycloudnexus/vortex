package com.consoleconnect.vortex.cc.model;

import lombok.Data;

@Data
public class CCClientProperty {
  private String baseUrl;
  private String adminApiKey;
  private String userApiKey;
  private String apiKeyName;
  private String companyUsername;
  private String companyId;
}
