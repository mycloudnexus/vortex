package com.consoleconnect.vortex.gateway.adapter.cc;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.RequestHelper;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

/** PUT /api/company/consolecore-poping-company/ports/orders */
@Slf4j
public class PortOrderCreateAdapter extends AbstractAdapter implements RouteAdapter {

  public PortOrderCreateAdapter(RouteAdapterContext context) {
    super(context);
  }

  /**
   * @param responseBody
   * @return
   */
  @Override
  public byte[] doProcess(ServerWebExchange exchange, byte[] responseBody) {

    String orgId = RequestHelper.getCurrentOrgId(exchange);

    String response = new String(responseBody, StandardCharsets.UTF_8);

    String orderId = JsonPathToolkit.read(response, "$.id");
    log.info("create vortex order, orderId:{}", orderId);

    context.getOrderService().createOrder(orgId, orderId, ResourceTypeEnum.PORT, null);
    return responseBody;
  }
}
