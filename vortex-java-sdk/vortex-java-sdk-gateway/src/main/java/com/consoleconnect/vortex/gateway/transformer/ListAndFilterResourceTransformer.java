package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListAndFilterResourceTransformer
    extends AbstractResourceTransformer<ListAndFilterResourceTransformer.Options> {

  public ListAndFilterResourceTransformer(
      OrganizationService organizationService, ResourceService resourceService) {
    super(Options.class, organizationService, resourceService);
  }

  @Override
  public String doTransform(String responseBody, TransformerContext<Options> context) {

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    // data to be filtered, it MUST be a list
    List<Object> data = ctx.read(context.getSpecification().getResponseDataPath());

    Object filteredData =
        this.filterData(
            data, context.getSpecification().getOptions().getFilter(), context.getVariables());

    if (TransformerSpecification.JSON_ROOT.equals(
        context.getSpecification().getResponseDataPath())) {
      ctx = JsonPathToolkit.createDocCtx(filteredData);
    } else {
      ctx.set(context.getSpecification().getResponseDataPath(), filteredData);
    }

    afterTransform(data, context);
    return ctx.jsonString();
  }

  private Object filterData(Object data, String filter, Map<String, Object> variables) {
    variables.put(VAR_DATA, data);
    return SpelExpressionEngine.evaluate(filter, variables, Object.class);
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCES_LIST_AND_FILTER;
  }

  @Data
  public static class Options {
    private String filter;
  }
}
