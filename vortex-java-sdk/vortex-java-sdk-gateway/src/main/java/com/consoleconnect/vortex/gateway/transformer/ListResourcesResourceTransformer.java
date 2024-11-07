package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.filter.ResponseBodyTransformerGatewayFilterFactory;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@AllArgsConstructor
@Service
@Slf4j
public class ListResourcesResourceTransformer extends AbstractResourceTransformer {

  private final OrderService orderService;

  public byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      ResponseBodyTransformerGatewayFilterFactory.Config config) {

    String orgId = userContext.getOrgId();

    // filter resource by organization
    //    Map<String, OrderEntity> resources =
    //        orderService.listResourceByType(orgId, config.getResourceType()).stream()
    //            .collect(Collectors.toMap(OrderEntity::getResourceId, x -> x));

    //    Set<String> resourceIds = resources.keySet();

    Set<String> resourceIds = new HashSet<>();

    String responseJson = new String(responseBody, StandardCharsets.UTF_8);

    DocumentContext dc = JsonPathToolkit.createDocCtx(responseJson);
    List<Map<String, Object>> resOrders = dc.read(config.getResponseBodyPath());

    // filter resources
    resOrders.removeIf(o -> filterResource(resourceIds, o));

    // override
    DocumentContext ctx = JsonPathToolkit.createDocCtx(resOrders);

    byte[] resBytes = ctx.jsonString().getBytes(StandardCharsets.UTF_8);
    log.info("process completed, resourceType:{}", config.getResourceType());
    return resBytes;
  }

  @Override
  public String getTransformerId() {
    return "resource.list";
  }

  // default filter
  protected boolean filterResource(Set<String> resourceIds, Map<String, Object> dto) {
    String oId = (String) dto.get("id");
    if (resourceIds.contains(oId)) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }
}
