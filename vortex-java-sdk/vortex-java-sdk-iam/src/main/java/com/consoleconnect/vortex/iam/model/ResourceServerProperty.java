package com.consoleconnect.vortex.iam.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@Data
public class ResourceServerProperty {

  private List<String> corsAllowedHeaders = List.of("*");
  private List<String> corsAllowedOrigins = List.of("*");
  private List<String> corsAllowedMethods = List.of("*");

  private List<String> defaultAllowedPaths =
      List.of(
          "/images/**",
          "/js/**",
          "/css/**",
          "/webjars/**",
          "/actuator/health",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/",
          "/favicon.ico",
          "/.well-known/**");

  private List<String> allowedPaths = List.of();
  private List<PathPermission> pathPermissions = List.of();

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class PathPermission {
    private List<HttpMethod> httpMethods;
    private String path;
    private List<String> roles;
  }
}
