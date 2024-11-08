package com.consoleconnect.vortex.gateway;

import static org.mockito.ArgumentMatchers.any;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.filter.MefAPIHeaderGatewayFilterFactory;
import com.consoleconnect.vortex.gateway.filter.ResponseBodyTransformerGatewayFilterFactory;
import com.consoleconnect.vortex.gateway.transformer.AbstractResourceTransformer;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.FixedLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@MockIntegrationTest
class GatewayFilterFactoryTest extends AbstractIntegrationTest {

  byte[] resBytes =
      new byte[] {
        31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -85, 86, -54, 76, 81, -78, 82, -54, 76, 49, 48, 48, 4,
        35, -91, 90, 0, -8, 26, 77, 18, 19, 0, 0, 0
      };

  @Autowired private Set<MessageBodyDecoder> messageBodyDecoders;
  @Autowired private Set<MessageBodyEncoder> messageBodyEncoders;
  @Autowired private List<AbstractResourceTransformer> transformers;

  private ServerWebExchange exchange;
  private ResponseBodyTransformerGatewayFilterFactory.Config config;
  private ResponseBodyTransformerGatewayFilterFactory responseTransformerFilter;
  private MefAPIHeaderGatewayFilterFactory mefAPIHeaderFilter;
  private GatewayFilterChain chain;

  @BeforeEach
  void setUp() {
    TransformerApiProperty property = new TransformerApiProperty();
    property.setHttpMethod(HttpMethod.PUT);
    property.setHttpPath("/test/api/do");
    property.setTransformer("resource.create");
    property.setResourceType(ResourceTypeEnum.PORT);

    List<TransformerApiProperty> apis = new ArrayList<>();
    apis.add(property);
    config = new ResponseBodyTransformerGatewayFilterFactory.Config(apis);

    responseTransformerFilter =
        new ResponseBodyTransformerGatewayFilterFactory(
            messageBodyDecoders, messageBodyEncoders, transformers);

    MockServerHttpRequest request =
        MockServerHttpRequest.put("/test/api/do").header("Authorization", "Bearer token").build();

    MockServerHttpResponse response = new MockServerHttpResponse();
    response.setStatusCode(HttpStatus.OK);
    response.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");

    exchange =
        new DefaultServerWebExchange(
            request,
            response,
            new DefaultWebSessionManager(),
            new DefaultServerCodecConfigurer(),
            new FixedLocaleContextResolver());

    UserContext userContext = new UserContext();
    userContext.setCustomerId("orgId");
    exchange.getAttributes().put(IamConstants.X_VORTEX_USER_CONTEXT, userContext);

    chain = Mockito.mock(GatewayFilterChain.class);

    Mockito.when(chain.filter(any()))
        .thenAnswer(
            invocation -> {
              ServerWebExchange ex = invocation.getArgument(0);

              DefaultDataBuffer buffer = new DefaultDataBufferFactory().wrap(resBytes);
              return ex.getResponse().writeWith(Flux.just(buffer));
            });
  }

  @Test
  void testResponseBodyTransformerGatewayFilterFactory() {
    GatewayFilter filter = responseTransformerFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);
    StepVerifier.create(result).expectComplete().verify();
  }

  @Test
  void testMefAPIHeaderGatewayFilterFactory() {
    MefAPIHeaderGatewayFilterFactory.Config mefConfig =
        new MefAPIHeaderGatewayFilterFactory.Config();
    mefConfig.setPrefix("/test");
    mefConfig.setKey("key");
    mefConfig.setKeyValue("value");
    mefAPIHeaderFilter = new MefAPIHeaderGatewayFilterFactory();
    GatewayFilter filter = mefAPIHeaderFilter.apply(mefConfig);
    Mono<Void> result = filter.filter(exchange, chain);
    StepVerifier.create(result).expectComplete().verify();
  }
}
