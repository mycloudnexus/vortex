package com.consoleconnect.vortex.gateway.filter;

import static java.util.function.Function.identity;

import com.consoleconnect.vortex.gateway.model.GatewayProperty;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.transformer.TransformerChain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final TransformerChain transformerChain;

  private final GatewayProperty gatewayProperty;

  public ResponseBodyTransformerGatewayFilterFactory(
      Set<MessageBodyDecoder> messageBodyDecoders,
      Set<MessageBodyEncoder> messageBodyEncoders,
      TransformerChain transformerChain,
      GatewayProperty gatewayProperty) {
    super(Config.class);
    this.gatewayProperty = gatewayProperty;
    this.messageBodyDecoders =
        messageBodyDecoders.stream()
            .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
    this.messageBodyEncoders =
        messageBodyEncoders.stream()
            .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
    this.transformerChain = transformerChain;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new ResponseBodyTransformerGatewayFilter(config);
  }

  public class ResponseBodyTransformerGatewayFilter implements GatewayFilter, Ordered {

    private final List<TransformerSpecification> transformerSpecifications;

    public ResponseBodyTransformerGatewayFilter(Config config) {
      if (config.getSpecifications().stream()
          .anyMatch(specification -> !specification.isValidated())) {
        log.error("transformer specification are invalid.");
        throw new IllegalArgumentException("transformer specification are invalid.");
      }
      transformerSpecifications =
          config.getSpecifications().stream()
              .peek(spec -> spec.setHttpPath(gatewayProperty.getPathPrefix() + spec.getHttpPath()))
              .toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      // Step 1: match transformer
      List<TransformerSpecification> specifications = findTransformerSpecifications(exchange);
      if (specifications.isEmpty()) {
        log.info(
            "{} {},no matched transformer specification, skip transform.",
            exchange.getRequest().getMethod(),
            exchange.getRequest().getURI().getPath());
        return chain.filter(exchange);
      }

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

                                      byte[] resBody = transform(exchange, content, specifications);

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

    private byte[] transform(
        ServerWebExchange exchange,
        byte[] resBytes,
        List<TransformerSpecification> specifications) {

      for (TransformerSpecification specification : specifications) {
        resBytes = transformerChain.transform(exchange, resBytes, specification);
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

    public List<TransformerSpecification> findTransformerSpecifications(
        ServerWebExchange exchange) {

      HttpMethod method = exchange.getRequest().getMethod();
      String path = exchange.getRequest().getURI().getPath();

      log.info("findTransformerSpecification method:{}, path:{}", method, path);
      return transformerSpecifications.stream()
          .filter(t -> t.getHttpMethod() == method && pathMatcher.match(t.getHttpPath(), path))
          .toList();
    }
  }

  @Data
  public static class Config {

    private List<TransformerSpecification> specifications = new ArrayList<>();
  }
}
