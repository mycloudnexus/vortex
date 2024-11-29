package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Data
@ToString
public class TransformerSpecificationInternal<T> {

  private HttpMethod httpMethod;
  private String httpPath;
  private TransformerIdentityEnum transformer;
  private ResourceTypeEnum resourceType;
  private String resourceInstanceId;
  private String responseDataPath;

  private T metadata;

  public static <T> TransformerSpecificationInternal<T> of(
      TransformerSpecification specification, Class<T> cls) {
    TransformerSpecificationInternal<T> transformerSpecificationInternal =
        new TransformerSpecificationInternal<>();
    transformerSpecificationInternal.setHttpMethod(specification.getHttpMethod());
    transformerSpecificationInternal.setHttpPath(specification.getHttpPath());
    transformerSpecificationInternal.setTransformer(specification.getTransformer());
    transformerSpecificationInternal.setResourceType(specification.getResourceType());
    transformerSpecificationInternal.setResourceInstanceId(specification.getResourceInstanceId());
    transformerSpecificationInternal.setResponseDataPath(specification.getResponseDataPath());

    T metadata = JsonToolkit.fromJson(JsonToolkit.toJson(specification.getMetadata()), cls);
    transformerSpecificationInternal.setMetadata(metadata);

    return transformerSpecificationInternal;
  }
}
