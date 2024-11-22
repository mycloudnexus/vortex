package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
public class DefaultResourceListTransformer extends AbstractResourceTransformer<Object> {

  protected final OrderService orderService;

  public DefaultResourceListTransformer(OrderService orderService) {
    super(Object.class);
    this.orderService = orderService;
  }

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      UserContext userContext,
      TransformerApiProperty config,
      Object metadata) {

    // filter resource by organization
    Map<String, OrderEntity> resources =
        orderService.listResourceByType(customerId, config.getResourceType()).stream()
            .collect(Collectors.toMap(OrderEntity::getResourceId, x -> x));

    Set<String> resourceIds = resources.keySet();

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> resOrders = ctx.read(config.getResponseBodyPath());

    // filter resources
    resOrders.removeIf(o -> filterResource(resourceIds, o, config));

    if (TransformerApiProperty.DEFAULT_BODY_PATH.equals(config.getResponseBodyPath())) {
      // override ctx
      ctx = JsonPathToolkit.createDocCtx(resOrders);
    } else {
      ctx.set(config.getResponseBodyPath(), resOrders);
    }

    log.info("process completed, resourceType:{}", config.getResourceType());
    return ctx.jsonString();
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
