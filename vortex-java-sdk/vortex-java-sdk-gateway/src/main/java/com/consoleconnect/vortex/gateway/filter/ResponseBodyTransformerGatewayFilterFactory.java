package com.consoleconnect.vortex.gateway.filter;

import static java.util.function.Function.identity;

import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.transformer.AbstractResourceTransformer;
import java.util.*;
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

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final Map<TransformerIdentityEnum, AbstractResourceTransformer<?>> transformerMap;

  public ResponseBodyTransformerGatewayFilterFactory(
      Set<MessageBodyDecoder> messageBodyDecoders,
      Set<MessageBodyEncoder> messageBodyEncoders,
      List<AbstractResourceTransformer<?>> transformers) {
    super(Config.class);
    this.messageBodyDecoders =
        messageBodyDecoders.stream()
            .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
    this.messageBodyEncoders =
        messageBodyEncoders.stream()
            .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
    this.transformerMap =
        transformers.stream()
            .collect(Collectors.toMap(AbstractResourceTransformer::getTransformerId, identity()));
  }

  public Optional<AbstractResourceTransformer<?>> findTransformer(
      TransformerIdentityEnum transformer) {
    return Optional.ofNullable(transformerMap.get(transformer));
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new ResponseBodyTransformerGatewayFilter(config);
  }

  public class ResponseBodyTransformerGatewayFilter implements GatewayFilter, Ordered {

    private final List<TransformerSpecification.Default> transformerSpecifications;

    public ResponseBodyTransformerGatewayFilter(Config config) {
      if (config.getSpecifications().stream()
          .anyMatch(specification -> !this.validate(specification))) {
        log.error("transformer specification are invalid.");
        throw new IllegalArgumentException("transformer specification are invalid.");
      }
      transformerSpecifications = config.getSpecifications();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      // Step 1: match transformer
      Optional<TransformerSpecification.Default> specificationOptional =
          findTransformerSpecification(exchange);
      if (specificationOptional.isEmpty()) {
        log.info("no matched transformer specification.");
        return chain.filter(exchange);
      }

      TransformerSpecification.Default specification = specificationOptional.get();
      final Optional<AbstractResourceTransformer<?>> transformer =
          findTransformer(specification.getTransformer());
      if (transformer.isEmpty()) {
        log.error("no matched transformer, transformer:{}", specification.getTransformer());
        return chain.filter(exchange);
      }

      log.info(
          "matched transformer:{}, specification:{}",
          transformer.get().getClass().getName(),
          specification);
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

                                      // Step 4: process the response body with the transformer
                                      byte[] resBody =
                                          transformer
                                              .get()
                                              .transform(exchange, content, specification);

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
        if (decoder != null) {
          log.info("extractBody encoding: {}, decoder{}", encoding, decoder.getClass());
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

    private boolean validate(TransformerSpecification.Default t) {
      return t.getHttpMethod() != null
          && t.getHttpPath() != null
          && t.getTransformer() != null
          && t.getResourceType() != null;
    }

    public Optional<TransformerSpecification.Default> findTransformerSpecification(
        ServerWebExchange exchange) {

      HttpMethod method = exchange.getRequest().getMethod();
      String path = exchange.getRequest().getURI().getPath();

      log.info("findTransformerSpecification method:{}, path:{}", method, path);
      return transformerSpecifications.stream()
          .filter(t -> t.getHttpMethod() == method && pathMatcher.match(t.getHttpPath(), path))
          .findFirst();
    }
  }

  @Data
  public static class Config {

    private List<TransformerSpecification.Default> specifications = new ArrayList<>();
  }
}
