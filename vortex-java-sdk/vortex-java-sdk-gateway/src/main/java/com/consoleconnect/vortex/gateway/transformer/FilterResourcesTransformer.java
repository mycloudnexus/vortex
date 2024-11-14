package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.jayway.jsonpath.DocumentContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
public class FilterResourcesTransformer
    extends AbstractResourceTransformer<FilterResourcesTransformer.Metadata> {

  private final ResourceService resourceService;

  public FilterResourcesTransformer(ResourceService resourceService) {
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

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> data = ctx.read(config.getResponseBodyPath());
    Object filteredData = this.filterData(metadata, createVariables(data, userContext, config));

    if (TransformerApiProperty.DEFAULT_BODY_PATH.equals(config.getResponseBodyPath())) {
      ctx = JsonPathToolkit.createDocCtx(filteredData);
    } else {
      ctx.set(config.getResponseBodyPath(), filteredData);
    }
    return ctx.jsonString();
  }

  private Object filterData(Metadata metadata, Map<String, Object> variables) {

    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariables(variables);
    ExpressionParser parser = new SpelExpressionParser();
    return parser.parseExpression(metadata.getFilter()).getValue(context);
  }

  public Map<String, Object> createVariables(
      Object data, UserContext userContext, TransformerApiProperty config) {
    // filter resource by organization
    List<ResourceEntity> resources =
        resourceService.findAllByOrganizationIdAndResourceType(
            userContext.getCustomerId(), config.getResourceType());

    List<String> orderIds = resources.stream().map(ResourceEntity::getOrderId).toList();
    List<String> resourceIds = resources.stream().map(ResourceEntity::getResourceId).toList();

    Map<String, Object> context = new HashMap<>();
    context.put("orderIds", orderIds);
    context.put("resourceIds", resourceIds);
    context.put("resources", resources);
    context.put("data", data);
    return context;
  }

  @Override
  public String getTransformerId() {
    return "resources.filter";
  }

  @Data
  public static class Metadata {
    private String filter;
  }
}
