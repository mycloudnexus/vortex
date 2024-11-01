package com.consoleconnect.vortex.gateway.config;

import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class RouteAdapterProperty {
  private HttpMethod method;
  private String routePath;
  private String adapterClassName;
}
