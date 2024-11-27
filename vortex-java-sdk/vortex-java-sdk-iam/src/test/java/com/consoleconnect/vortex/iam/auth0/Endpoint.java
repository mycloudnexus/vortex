package com.consoleconnect.vortex.iam.auth0;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class Endpoint {
  private HttpMethod httpMethod;
  private String path;
}
