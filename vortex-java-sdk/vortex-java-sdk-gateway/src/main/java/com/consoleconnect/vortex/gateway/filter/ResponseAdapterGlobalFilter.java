package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResponseAdapterGlobalFilter implements GlobalFilter, Ordered {

  private final RouteAdapterFactory adapterFactory;

  public ResponseAdapterGlobalFilter(RouteAdapterFactory adapterFactory) {
    this.adapterFactory = adapterFactory;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // Step 1: match adapter
    RouteAdapter<byte[]> adapter = adapterFactory.matchAdapter(exchange);
    if (adapter == null) {
      // There may be data loss, or data security issues
      // ignore
      return chain.filter(exchange);
    }

    // Step 2: process the response body with the adapter
    ServerHttpResponseDecorator responseDecorator =
        new ServerHttpResponseDecorator(exchange.getResponse()) {
          @Override
          public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            if (getStatusCode() != null && getStatusCode().is2xxSuccessful()) {
              Flux<? extends DataBuffer> fluxBody = Flux.from(body);
              Flux<DataBuffer> modifiedBody =
                  fluxBody.map(
                      dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        byte[] resBody = adapter.process(exchange, content);
                        return bufferFactory().wrap(resBody);
                      });

              return super.writeWith(modifiedBody);
            }
            return super.writeWith(body);
          }
        };

    return chain.filter(exchange.mutate().response(responseDecorator).build());
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
