package com.consoleconnect.vortex.gateway.config;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class EndpointProperty {
  private HttpMethod method;
  private String routePath;
  private ResourceTypeEnum resourceType;
}
