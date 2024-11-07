package com.consoleconnect.vortex.gateway.adapter.cc;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.RequestHelper;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class DefaultResourceListAdapter extends AbstractAdapter implements RouteAdapter {

  public DefaultResourceListAdapter(RouteAdapterContext context) {
    super(context);
  }

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public byte[] doProcess(ServerWebExchange exchange, byte[] responseBody) {

    String orgId = RequestHelper.getCurrentOrgId(exchange);

    // filter resource by organization
    Map<String, OrderEntity> resources =
        context.getOrderService().listResourceByType(orgId, this.resourceType).stream()
            .collect(Collectors.toMap(OrderEntity::getResourceId, x -> x));

    Set<String> resourceIds = resources.keySet();

    String responseJson = new String(responseBody, StandardCharsets.UTF_8);

    DocumentContext dc = JsonPathToolkit.createDocCtx(responseJson);
    List<Map<String, Object>> resOrders = dc.read(context.getApiProperty().getResponseRootPath());

    // filter resources
    resOrders.removeIf(o -> filterResource(resourceIds, o));

    // override
    DocumentContext ctx = JsonPathToolkit.createDocCtx(resOrders);

    byte[] resBytes = ctx.jsonString().getBytes(StandardCharsets.UTF_8);
    log.info("process completed, resourceType:{}", resourceType);
    return resBytes;
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
