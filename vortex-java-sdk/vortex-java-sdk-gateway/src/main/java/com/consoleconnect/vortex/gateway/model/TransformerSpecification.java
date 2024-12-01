package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  private T options;

  @JsonProperty("if")
  private String conditionOn;

  public <R> TransformerSpecification<R> copy(Class<R> cls) {
    TransformerSpecification<R> transformerSpecification = new TransformerSpecification<>();
    transformerSpecification.setHttpMethod(this.getHttpMethod());
    transformerSpecification.setHttpPath(this.getHttpPath());
    transformerSpecification.setTransformer(this.getTransformer());
    transformerSpecification.setResourceType(this.getResourceType());
    transformerSpecification.setResourceInstanceId(this.getResourceInstanceId());
    transformerSpecification.setResponseDataPath(this.getResponseDataPath());
    transformerSpecification.setConditionOn(this.getConditionOn());
    if (this.getOptions() == null) {
      return transformerSpecification;
    }
    R renderedOptions = JsonToolkit.fromJson(JsonToolkit.toJson(this.getOptions()), cls);
    transformerSpecification.setOptions(renderedOptions);
    return transformerSpecification;
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
