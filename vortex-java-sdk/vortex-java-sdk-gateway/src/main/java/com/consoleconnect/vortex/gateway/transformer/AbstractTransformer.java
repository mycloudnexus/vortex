package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTransformer<T> {

  public static final String VAR_DATA = "data";
  public static final String VAR_CUSTOMER_NAME = "customerName";

  protected final ResourceService resourceService;
  protected final OrganizationService organizationService;

  protected AbstractTransformer(
      OrganizationService organizationService, ResourceService resourceService) {
    this.organizationService = organizationService;
    this.resourceService = resourceService;
  }

  protected abstract String doTransform(
      String data, TransformerContext context, TransformerSpecification.TransformerChain<T> chain);

  public abstract TransformerIdentityEnum getTransformerId();

  protected String extraData(String responseBody, String responseDataPath) {
    return TransformerSpecification.JSON_ROOT.equalsIgnoreCase(responseDataPath)
        ? responseBody
        : JsonToolkit.toJson(JsonPathToolkit.read(responseBody, responseDataPath, Object.class));
  }

  protected void createResource(CreateResourceRequest request) {
    this.resourceService.create(request, null);
  }
}
