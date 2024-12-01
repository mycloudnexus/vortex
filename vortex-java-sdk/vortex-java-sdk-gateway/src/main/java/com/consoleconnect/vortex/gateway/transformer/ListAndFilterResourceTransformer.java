package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.jayway.jsonpath.DocumentContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListAndFilterResourceTransformer
    extends AbstractResourceTransformer<ListAndFilterResourceTransformer.Options> {

  private final ResourceService resourceService;

  public ListAndFilterResourceTransformer(ResourceService resourceService) {
    super(Options.class);
    this.resourceService = resourceService;
  }

  @Override
  public String doTransform(String responseBody, TransformerContext<Options> context) {

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    // data to be filtered, it MUST be a list
    List<Object> data = ctx.read(context.getSpecification().getResponseDataPath());

    // variables for filtering
    Map<String, Object> filterVariables =
        buildFilterVariables(context.getCustomerId(), context.getSpecification().getResourceType());

    Object filteredData =
        this.filterData(data, context.getSpecification().getOptions().getFilter(), filterVariables);

    if (TransformerSpecification.JSON_ROOT.equals(
        context.getSpecification().getResponseDataPath())) {
      ctx = JsonPathToolkit.createDocCtx(filteredData);
    } else {
      ctx.set(context.getSpecification().getResponseDataPath(), filteredData);
    }
    return ctx.jsonString();
  }

  private Object filterData(Object data, String filter, Map<String, Object> variables) {
    variables.put(VAR_DATA, data);
    return SpelExpressionEngine.evaluate(filter, variables, Object.class);
  }

  public Map<String, Object> buildFilterVariables(String customerId, String resourceType) {
    // filter resource by customerId and resourceType
    List<ResourceEntity> resources =
        resourceService.findAllByCustomerIdAndResourceType(customerId, resourceType);

    List<String> orderIds =
        resources.stream().map(ResourceEntity::getOrderId).filter(Objects::nonNull).toList();

    List<String> resourceIds =
        resources.stream().map(ResourceEntity::getResourceId).filter(Objects::nonNull).toList();

    Map<String, Object> context = new HashMap<>();
    context.put(VAR_ORDER_IDS, orderIds);
    context.put(VAR_RESOURCE_IDS, resourceIds);
    context.put(VAR_RESOURCES, resources);
    return context;
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
