package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateResourceTransformer
    extends AbstractResourceTransformer<CreateResourceTransformer.Metadata> {

  protected final ResourceService resourceService;

  public CreateResourceTransformer(ResourceService resourceService) {
    super(Metadata.class);
    this.resourceService = resourceService;
  }

  @Override
  public String doTransform(String responseBody, TransformerContext<Metadata> context) {

    CreateResourceRequest request = new CreateResourceRequest();
    request.setCustomerId(context.getCustomerId());
    request.setResourceType(context.getSpecification().getResourceType());

    String data =
        JsonPathToolkit.read(responseBody, context.getSpecification().getResponseDataPath());

    Metadata metadata = context.getSpecification().getMetadata();
    if (metadata.getOrderId() != null) {
      request.setOrderId(JsonPathToolkit.read(data, metadata.getOrderId()));
    }
    if (metadata.getResourceId() != null) {
      request.setResourceId(JsonPathToolkit.read(data, metadata.getResourceId()));
    }
    resourceService.create(request);
    return responseBody;
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCE_CREATE;
  }

  @Data
  public static class Metadata {
    private String orderId;
    private String resourceId;
  }
}
