package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Data
@ToString
public class TransformerSpecification {

  public static final String JSON_ROOT = "$";

  private HttpMethod httpMethod;
  private String httpPath;
  private TransformerIdentityEnum transformer;
  private ResourceTypeEnum resourceType;
  private String resourceInstanceId = "id";
  private String responseDataPath = JSON_ROOT;

  private Map<String, Object> metadata = new HashMap<>();
}
