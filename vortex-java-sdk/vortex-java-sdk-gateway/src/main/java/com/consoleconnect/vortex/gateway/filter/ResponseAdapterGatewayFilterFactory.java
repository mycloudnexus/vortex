package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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
    return new ResponseAdapterGatewayFilter(adapterFactory);
  }

  public class ResponseAdapterGatewayFilter implements GatewayFilter, Ordered {

    private final RouteAdapterFactory adapterFactory;

    public ResponseAdapterGatewayFilter(RouteAdapterFactory adapterFactory) {
      this.adapterFactory = adapterFactory;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      RouteAdapter adapter = adapterFactory.matchAdapter(exchange);
      if (adapter == null) {
        return chain.filter(exchange);
      }

      log.info("matchAdapter:{}", adapter.getClass().getName());
      return chain.filter(
          exchange
              .mutate()
              .response(
                  new ServerHttpResponseDecorator(exchange.getResponse()) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                      HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                      if (statusCode != null && statusCode.is2xxSuccessful()) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                        return super.writeWith(
                            fluxBody
                                .buffer()
                                .map(
                                    dataBuffers -> {
                                      ByteArrayOutputStream outputStream =
                                          new ByteArrayOutputStream();
                                      dataBuffers.stream()
                                          .forEach(
                                              buffer -> {
                                                byte[] bytes = new byte[buffer.readableByteCount()];
                                                buffer.read(bytes);
                                                DataBufferUtils.release(buffer);
                                                try {
                                                  outputStream.write(bytes);
                                                } catch (IOException e) {
                                                  log.info("response body read bytes failed", e);
                                                }
                                              });

                                      byte[] originalBody = outputStream.toByteArray();
                                      byte[] resBody = adapter.process(exchange, originalBody);
                                      return bufferFactory().wrap(resBody);
                                    }));
                      }
                      return super.writeWith(body);
                    }
                  })
              .build());
    }

    @Override
    public int getOrder() {
      return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
  }
}
