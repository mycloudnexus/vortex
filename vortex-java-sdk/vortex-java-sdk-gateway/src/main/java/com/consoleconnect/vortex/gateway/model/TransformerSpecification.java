package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Data
@Slf4j
public class TransformerSpecification {

  public static final String JSON_ROOT = "$";

  private HttpMethod httpMethod;
  private String httpPath;
  private String resourceType;
  private String resourceInstanceId = "id";
  private String responseDataPath = JSON_ROOT;

  private List<TransformerChain<?>> transformerChains;

  private String when;

  private List<String> required = List.of("httpMethod", "httpPath", "resourceType");

  public TransformerSpecification copy() {
    TransformerSpecification transformerSpecification = new TransformerSpecification();
    transformerSpecification.setHttpMethod(this.getHttpMethod());
    transformerSpecification.setHttpPath(this.getHttpPath());
    transformerSpecification.setResourceType(this.getResourceType());
    transformerSpecification.setResourceInstanceId(this.getResourceInstanceId());
    transformerSpecification.setResponseDataPath(this.getResponseDataPath());
    transformerSpecification.setWhen(this.getWhen());

    transformerSpecification.setTransformerChains(this.transformerChains);
    return transformerSpecification;
  }

  public boolean isValidated() {

    for (String name : required) {
      try {
        Field field = TransformerSpecification.class.getDeclaredField(name);
        if (field.get(this) == null) {
          return false;
        }
      } catch (Exception e) {
        log.error("Error validating transformer specification", e);
        return false;
      }
    }

    return true;
  }

  @Data
  public static class TransformerChain<O> {
    private TransformerIdentityEnum chainName;
    private Map<String, Object> options;
    private List<Hook.Default> beforeTransformHooks;
    private List<Hook.Default> afterTransformHooks;

    public O getChanOptions(Class<O> cls) {
      return JsonToolkit.fromJson(JsonToolkit.toJson(this.getOptions()), cls);
    }
  }
}
