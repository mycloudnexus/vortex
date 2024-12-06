package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import java.nio.charset.StandardCharsets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateResourceTransformerChain
    extends AbstractTransformerChain<CreateResourceTransformerChain.Options> {

  public CreateResourceTransformerChain(
      OrganizationService organizationService, ResourceService resourceService) {
    super(organizationService, resourceService);
  }

  @Override
  public byte[] doTransform(
      byte[] responseBody,
      TransformerContext context,
      TransformerSpecification.TransformerChain<CreateResourceTransformerChain.Options> chain) {

    CreateResourceRequest request = new CreateResourceRequest();
    request.setCustomerId(context.getCustomerId());
    request.setResourceType(context.getSpecification().getResourceType());

    String data =
        extraData(
            new String(responseBody, StandardCharsets.UTF_8),
            context.getSpecification().getResponseDataPath());

    Options options = chain.getChanOptions(Options.class);
    if (options.getOrderId() != null) {
      request.setOrderId(JsonPathToolkit.read(data, options.getOrderId()));
    }
    if (options.getResourceId() != null) {
      request.setResourceId(JsonPathToolkit.read(data, options.getResourceId()));
    }
    createResource(request);
    return responseBody;
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCE_CREATE;
  }

  @Data
  public static class Options {
    private String orderId;
    private String resourceId;
  }
}
