package com.consoleconnect.vortex.gateway.adapter.cc;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.RequestHelper;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class DefaultResourceOrderCreateAdapter extends AbstractAdapter implements RouteAdapter {

  public DefaultResourceOrderCreateAdapter(RouteAdapterContext context) {
    super(context);
    Assert.notNull(context.getResourceType(), "resourceType must not be null");
    this.resourceType = context.getResourceType();
  }

  /**
   * @param responseBody
   * @return
   */
  @Override
  public byte[] doProcess(ServerWebExchange exchange, byte[] responseBody) {

    String orgId = RequestHelper.getCurrentOrgId(exchange);

    String response = new String(responseBody, StandardCharsets.UTF_8);

    String orderId = null;
    String resourceId = JsonPathToolkit.read(response, "$.id");
    log.info(
        "create vortex resource order, resourceType:{}, resourceId:{}", resourceType, resourceId);

    // order
    if (this.resourceType == ResourceTypeEnum.PORT) {
      orderId = resourceId;
      resourceId = null;
    }

    context.getOrderService().createOrder(orgId, orderId, this.resourceType, resourceId);
    return responseBody;
  }
}
