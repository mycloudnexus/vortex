package com.consoleconnect.vortex.gateway.adapter.cc;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public abstract class AbstractAdapter implements RouteAdapter<byte[]> {

  protected RouteAdapterContext context;

  public AbstractAdapter(RouteAdapterContext context) {
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
    } catch (Throwable t) {
      log.error("{} process error.", getClass().getSimpleName(), t);
      throw VortexException.badRequest("Failed to process", t);
    } finally {
      log.info(
          "{} process cost: {} ms.",
          getClass().getSimpleName(),
          System.currentTimeMillis() - start);
    }
  }

  protected abstract byte[] doProcess(ServerWebExchange exchange, byte[] responseBody);
}
