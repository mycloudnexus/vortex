package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultResourceListTransformer extends AbstractResourceTransformer {

  protected final OrderService orderService;

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      UserContext userContext,
      TransformerApiProperty config) {

    String orgId = userContext.getCustomerId();

    // filter resource by organization
    Map<String, OrderEntity> resources =
        orderService.listResourceByType(orgId, config.getResourceType()).stream()
            .collect(Collectors.toMap(OrderEntity::getResourceId, x -> x));

    Set<String> resourceIds = resources.keySet();

    String responseJson = new String(responseBody, StandardCharsets.UTF_8);

    DocumentContext dc = JsonPathToolkit.createDocCtx(responseJson);
    List<Map<String, Object>> resOrders = dc.read(config.getResponseBodyPath());

    // filter resources
    resOrders.removeIf(o -> filterResource(resourceIds, o, config));

    // override
    DocumentContext ctx = JsonPathToolkit.createDocCtx(resOrders);

    byte[] resBytes = ctx.jsonString().getBytes(StandardCharsets.UTF_8);
    log.info("process completed, resourceType:{}", config.getResourceType());
    return resBytes;
  }

  // default filter
  protected boolean filterResource(
      Set<String> resourceIds, Map<String, Object> dto, TransformerApiProperty config) {
    String oId = (String) dto.get(config.getResourceInstanceId());
    if (resourceIds.contains(oId)) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  @Override
  public String getTransformerId() {
    return "resource.list";
  }
}
