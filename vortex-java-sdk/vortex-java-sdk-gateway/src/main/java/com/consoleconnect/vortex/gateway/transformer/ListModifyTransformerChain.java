package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.dto.OrganizationInfo;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListModifyTransformerChain
    extends AbstractTransformerChain<ListModifyTransformerChain.Options> {

  protected ListModifyTransformerChain(
      OrganizationService organizationService, ResourceService resourceService) {
    super(organizationService, resourceService);
  }

  @Override
  public byte[] doTransform(
      byte[] responseBody,
      TransformerContext context,
      TransformerSpecification.TransformerChain<ListModifyTransformerChain.Options> chain) {

    Options options = chain.getChanOptions(Options.class);
    if (options.getWhen() != null
        && Boolean.FALSE.equals(
            SpelExpressionEngine.evaluate(
                options.getWhen(), context.getVariables(), Boolean.class))) {
      log.info("variables:{}", context.getVariables());
      log.info("Skip modify resource, as when condition is not met,{}", options.getWhen());
      return responseBody;
    }

    String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);
    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBodyStr);
    // data to be filtered, it MUST be a list
    List<Object> data = ctx.read(context.getSpecification().getResponseDataPath());

    OrganizationInfo org = organizationService.findOne(context.getCustomerId());
    context.getVariables().put(VAR_CUSTOMER_NAME, org.getName());

    for (int i = 0; i < data.size(); i++) {
      for (Map.Entry<String, Object> property : options.getModifier().entrySet()) {
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

    return ctx.jsonString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCES_LIST_AND_MODIFY;
  }

  @Data
  public static class Options {
    private String when;
    private Map<String, Object> modifier;
  }
}
