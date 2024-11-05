package com.consoleconnect.vortex.gateway.filter;

import static java.util.function.Function.identity;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
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
  private final Map<String, MessageBodyDecoder> messageBodyDecoders;
  private final Map<String, MessageBodyEncoder> messageBodyEncoders;

  public ResponseAdapterGatewayFilterFactory(
      RouteAdapterFactory adapterFactory,
      Set<MessageBodyDecoder> messageBodyDecoders,
      Set<MessageBodyEncoder> messageBodyEncoders) {
    super(NameConfig.class);
    this.adapterFactory = adapterFactory;
    this.messageBodyDecoders =
        messageBodyDecoders.stream()
            .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
    this.messageBodyEncoders =
        messageBodyEncoders.stream()
            .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
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
      // Step 1: match adapter
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
                                    // Step 2: read response body
                                    dataBuffers -> {
                                      DataBufferFactory bufFactory = new DefaultDataBufferFactory();
                                      DataBuffer buffer = bufFactory.join(dataBuffers);

                                      byte[] content = new byte[buffer.readableByteCount()];
                                      buffer.read(content);
                                      DataBufferUtils.release(buffer);

                                      // Step 3: decode
                                      content = extractBody(exchange, content);

                                      // Step 4: process the response body with the adapter
                                      byte[] resBody = adapter.process(exchange, content);

                                      // Step 5: encode
                                      return bufferFactory().wrap(writeBody(exchange, resBody));
                                    }));
                      }
                      return super.writeWith(body);
                    }
                  })
              .build());
    }

    private byte[] extractBody(ServerWebExchange exchange, byte[] resBytes) {

      List<String> encodingHeaders =
          exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
      for (String encoding : encodingHeaders) {
        MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
        log.info("extractBody encoding: {}, decoder{}", encoding, decoder.getClass());
        if (decoder != null) {
          return decoder.decode(resBytes);
        }
      }
      return resBytes;
    }

    private byte[] writeBody(ServerWebExchange exchange, byte[] resBytes) {
      List<String> encodingHeaders =
          exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
      for (String encoding : encodingHeaders) {
        MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
        if (encoder != null) {
          log.info("extractBody encoding: {}, encoder{}", encoding, encoder.getClass());
          DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
          return encoder.encode(bufferFactory.wrap(resBytes));
        }
      }

      return resBytes;
    }

    @Override
    public int getOrder() {
      return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
  }
}
