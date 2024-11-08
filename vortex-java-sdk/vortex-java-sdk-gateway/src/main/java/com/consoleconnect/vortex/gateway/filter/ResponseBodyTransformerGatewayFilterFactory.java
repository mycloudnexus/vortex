package com.consoleconnect.vortex.gateway.filter;

import static java.util.function.Function.identity;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.transformer.AbstractResourceTransformer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResponseBodyTransformerGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ResponseBodyTransformerGatewayFilterFactory.Config> {

  private final Map<String, MessageBodyDecoder> messageBodyDecoders;
  private final Map<String, MessageBodyEncoder> messageBodyEncoders;

  private final List<AbstractResourceTransformer> transformers;

  public ResponseBodyTransformerGatewayFilterFactory(
      Set<MessageBodyDecoder> messageBodyDecoders,
      Set<MessageBodyEncoder> messageBodyEncoders,
      List<AbstractResourceTransformer> transformers) {
    super(Config.class);
    this.messageBodyDecoders =
        messageBodyDecoders.stream()
            .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
    this.messageBodyEncoders =
        messageBodyEncoders.stream()
            .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
    this.transformers = transformers;
  }

  public Optional<AbstractResourceTransformer> findTransformer(String transformer) {
    if (transformer == null) {
      return Optional.empty();
    }
    return transformers.stream().filter(t -> t.getTransformerId().equals(transformer)).findFirst();
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new ResponseBodyTransformerGatewayFilter(config);
  }

  public class ResponseBodyTransformerGatewayFilter implements GatewayFilter, Ordered {

    private final Config config;

    public ResponseBodyTransformerGatewayFilter(Config config) {
      this.config = config;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      // Step 1: match transformer
      TransformerApiProperty apiProperty = config.match(exchange);
      if (apiProperty == null) {
        return chain.filter(exchange);
      }

      final Optional<AbstractResourceTransformer> transformer =
          findTransformer(apiProperty.getTransformer());
      if (transformer.isEmpty()) {
        return chain.filter(exchange);
      }

      log.info(
          "match transformer:{}, property:{}", transformer.get().getClass().getName(), apiProperty);
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
                                      byte[] resBody =
                                          transformer
                                              .get()
                                              .transform(exchange, content, apiProperty);

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

  @Data
  public static class Config {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private Map<String, TransformerApiProperty> apiTransformers;

    public Config(List<TransformerApiProperty> apis) {
      this.apiTransformers =
          apis.stream()
              .filter(
                  t -> {
                    if (check(t)) {
                      throw new IllegalArgumentException(
                          "transformer api properties cannot be empty.");
                    }
                    return Boolean.TRUE;
                  })
              .collect(
                  Collectors.toMap(t -> buildFullPath(t.getHttpMethod(), t.getHttpPath()), x -> x));
    }

    private boolean check(TransformerApiProperty t) {
      return t.getHttpMethod() == null
          || t.getHttpPath() == null
          || t.getTransformer() == null
          || t.getResourceType() == null;
    }

    public TransformerApiProperty match(ServerWebExchange exchange) {
      String key =
          buildFullPath(
              exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath());

      for (Map.Entry<String, TransformerApiProperty> entry : apiTransformers.entrySet()) {
        if (pathMatcher.match(entry.getKey(), key)) {
          return entry.getValue();
        }
      }
      return null;
    }

    private static String buildFullPath(HttpMethod method, String httpPath) {
      return method.name() + " " + httpPath;
    }
  }
}
