package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class DownstreamProperty {
  private String baseUrl;

  private String tokenHeaderName = "Authorization";
  private String token;
  private String tokenPrefix = "Bearer ";

  private Company company;

  @Data
  public static class Company {
    private String id;
    private String username;
    private String adminUserId;
  }
}
