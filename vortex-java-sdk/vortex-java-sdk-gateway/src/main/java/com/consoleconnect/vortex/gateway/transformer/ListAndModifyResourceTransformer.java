package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.dto.OrganizationInfo;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListAndModifyResourceTransformer
    extends AbstractResourceTransformer<ListAndModifyResourceTransformer.Options> {

  public ListAndModifyResourceTransformer(
      OrganizationService organizationService, ResourceService resourceService) {
    super(Options.class, organizationService, resourceService);
  }

  @Override
  public String doTransform(String responseBody, TransformerContext<Options> context) {

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    // data to be filtered, it MUST be a list
    List<Object> data = ctx.read(context.getSpecification().getResponseDataPath());

    for (int i = 0; i < data.size(); i++) {
      for (Map.Entry<String, Object> property :
          context.getSpecification().getOptions().entrySet()) {
        String path =
            String.format(
                "%s[%d].%s",
                context.getSpecification().getResponseDataPath(), i, property.getKey());
        Object updatingValue =
            SpelExpressionEngine.evaluate(
                property.getValue(), context.getVariables(), Object.class);
        String mainJson = JsonToolkit.toJson(ctx.read(path));
        String updatingJson = JsonToolkit.toJson(updatingValue);
        String updatedJson =
            JsonToolkit.merge(mainJson, updatingJson)
                .orElseThrow(() -> VortexException.internalError("Failed to merge json"));
        ctx.set(path, JsonToolkit.fromJson(updatedJson, Object.class));
      }
    }

    return ctx.jsonString();
  }

  @Override
  public Map<String, Object> buildVariables(TransformerContext<Options> context) {
    // filter resource by customerId and resourceType
    Map<String, Object> variables = super.buildVariables(context);

    OrganizationInfo org = organizationService.findOne(context.getCustomerId());
    variables.put(VAR_CUSTOMER_NAME, org.getName());
    return variables;
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCES_LIST_AND_MODIFY;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class Options extends HashMap<String, Object> {}
}
