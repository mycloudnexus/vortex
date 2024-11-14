package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.iam.model.UserContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

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
  public String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      UserContext userContext,
      TransformerApiProperty config,
      Metadata metadata) {

    CreateResourceRequest request = new CreateResourceRequest();
    request.setOrganizationId(userContext.getCustomerId());
    request.setResourceType(config.getResourceType());

    if (metadata.getOrderId() != null) {
      request.setOrderId(readJsonPath(responseBody, metadata.getOrderId(), config));
    }
    if (metadata.getResourceId() != null) {
      request.setResourceId(readJsonPath(responseBody, metadata.getResourceId(), config));
    }
    request.setSyncResourceConfig(metadata.getSyncResourceConfig());
    resourceService.create(request);
    return responseBody;
  }

  @Override
  public String getTransformerId() {
    return "resource.create";
  }

  @Data
  public static class Metadata {
    private String orderId;
    private String resourceId;
    private ResourceEntity.SyncResourceConfig syncResourceConfig;
  }
}
