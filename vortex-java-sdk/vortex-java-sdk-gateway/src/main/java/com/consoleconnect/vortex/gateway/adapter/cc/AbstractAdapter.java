package com.consoleconnect.vortex.gateway.adapter.cc;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractAdapter implements RouteAdapter {

  protected ResourceTypeEnum resourceType;
  protected RouteAdapterContext context;

  protected AbstractAdapter(RouteAdapterContext context) {
    Assert.notNull(context.getOrderService(), "orderService must not be null");
    this.context = context;
  }

  /**
   * @param exchange
   * @param responseBody
   * @return
   */
  @Override
  public byte[] process(ServerWebExchange exchange, byte[] responseBody) {
    long start = System.currentTimeMillis();
    try {
      return doProcess(exchange, responseBody);
    } catch (Exception e) {
      log.error("{} process error.", getClass().getSimpleName(), e);
      throw VortexException.badRequest("Failed to process", e);
    } finally {
      log.info(
          "{} process cost: {} ms.",
          getClass().getSimpleName(),
          System.currentTimeMillis() - start);
    }
  }

  protected abstract byte[] doProcess(ServerWebExchange exchange, byte[] responseBody);
}
