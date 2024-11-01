package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResponseAdapterGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

  private final RouteAdapterFactory adapterFactory;

  public ResponseAdapterGatewayFilterFactory(RouteAdapterFactory adapterFactory) {
    super(NameConfig.class);
    this.adapterFactory = adapterFactory;
  }

  @Override
  public GatewayFilter apply(NameConfig config) {
    return (exchange, chain) -> {
      // Step 1: match adapter
      RouteAdapter adapter = adapterFactory.matchAdapter(exchange);
      if (adapter == null) {
        return chain.filter(exchange);
      }

      // Step 2: process the response body with the adapter
      ServerHttpResponseDecorator responseDecorator =
          new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
              HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
              if (statusCode != null && statusCode.is2xxSuccessful()) {
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
    };
  }
}
