package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class DownstreamProperty {
  private String baseUrl;
  private String apiKeyName = "Authorization";
  private String userApiKey;

  private Company company;

  @Data
  public static class Company {
    private String id;
    private String username;
    private String adminUserId;
  }
}
