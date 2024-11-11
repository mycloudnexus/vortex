package com.consoleconnect.vortex.gateway.config;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Data
@ToString
public class TransformerApiProperty {
  private HttpMethod httpMethod;
  private String httpPath;
  private String transformer;
  private ResourceTypeEnum resourceType;
  private String resourceInstanceId = "id";
  private String responseBodyPath = "$";
}
