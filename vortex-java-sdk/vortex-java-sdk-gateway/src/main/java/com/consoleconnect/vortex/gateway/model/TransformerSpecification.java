package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Data
@Slf4j
public class TransformerSpecification<T> {

  public static final String JSON_ROOT = "$";

  private HttpMethod httpMethod;
  private String httpPath;
  private TransformerIdentityEnum transformer;
  private String resourceType;
  private String resourceInstanceId = "id";
  private String responseDataPath = JSON_ROOT;

  private List<Hook.Default> hooks;

  private T options;

  private String when;

  private List<String> required = List.of("httpMethod", "httpPath", "transformer", "resourceType");

  public <R> TransformerSpecification<R> copy(Class<R> cls) {
    TransformerSpecification<R> transformerSpecification = new TransformerSpecification<>();
    transformerSpecification.setHttpMethod(this.getHttpMethod());
    transformerSpecification.setHttpPath(this.getHttpPath());
    transformerSpecification.setTransformer(this.getTransformer());
    transformerSpecification.setResourceType(this.getResourceType());
    transformerSpecification.setResourceInstanceId(this.getResourceInstanceId());
    transformerSpecification.setResponseDataPath(this.getResponseDataPath());
    transformerSpecification.setWhen(this.getWhen());
    transformerSpecification.setHooks(this.getHooks());
    if (this.getOptions() == null) {
      return transformerSpecification;
    }
    R renderedOptions = JsonToolkit.fromJson(JsonToolkit.toJson(this.getOptions()), cls);
    transformerSpecification.setOptions(renderedOptions);
    return transformerSpecification;
  }

  public boolean isValidated() {

    return required.stream()
        .allMatch(
            field -> {
              if ("HttpMethod".equals(field)) {
                return this.getHttpMethod() != null;
              }
              if ("httpPath".equals(field)) {
                return this.getHttpPath() != null;
              }
              if ("transformer".equals(field)) {
                return this.getTransformer() != null;
              }
              if ("resourceType".equals(field)) {
                return this.getResourceType() != null;
              }
              if ("resourceInstanceId".equals(field)) {
                return this.getResourceInstanceId() != null;
              }
              if ("responseDataPath".equals(field)) {
                return this.getResponseDataPath() != null;
              }
              if ("when".equals(field)) {
                return this.getWhen() != null;
              }

              if (field.startsWith("options")) {
                return this.getOptions() != null;
              }
              return true;
            });
  }

  public static class Default extends TransformerSpecification<Map<String, Object>> {

    public Map<String, Object> getOptions() {
      if (super.getOptions() == null) {
        return Map.of();
      }
      return super.getOptions();
    }
  }
}
