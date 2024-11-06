package com.consoleconnect.vortex.gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterContext;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import com.consoleconnect.vortex.gateway.adapter.cc.PortOrderCreateAdapter;
import com.consoleconnect.vortex.gateway.filter.ResponseAdapterGatewayFilterFactory;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
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

  @Autowired private RouteAdapterFactory adapterFactory;
  @Autowired private Set<MessageBodyDecoder> messageBodyDecoders;
  @Autowired private Set<MessageBodyEncoder> messageBodyEncoders;

  private ServerWebExchange exchange;
  private AbstractGatewayFilterFactory.NameConfig config =
      new AbstractGatewayFilterFactory.NameConfig();
  private ResponseAdapterGatewayFilterFactory filterFactory;
  private GatewayFilterChain chain;

  @BeforeEach
  void setUp() {
    config.setName("name");
    filterFactory =
        new ResponseAdapterGatewayFilterFactory(
            adapterFactory, messageBodyDecoders, messageBodyEncoders);

    MockServerHttpRequest request =
        MockServerHttpRequest.put(
                "/consoleconnect/api/company/consolecore-poping-company/ports/orders")
            .header("Authorization", "Bearer token")
            .build();

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
  void testResponseAdapterGatewayFilterFactory() {
    GatewayFilter filter = filterFactory.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);
    StepVerifier.create(result).expectComplete().verify();
  }

  @Test
  void testCreateAdapter() {
    RouteAdapter adapter = adapterFactory.matchAdapter(exchange);
    byte[] resBytes = "{\"id\":\"id001\"}".getBytes();
    byte[] ret = adapter.process(exchange, resBytes);
    Assertions.assertNotNull(ret);
  }

  @Test
  void testAbnormalCase() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/abnormal/test").build();
    ServerWebExchange se =
        new DefaultServerWebExchange(
            request,
            new MockServerHttpResponse(),
            new DefaultWebSessionManager(),
            new DefaultServerCodecConfigurer(),
            new FixedLocaleContextResolver());
    RouteAdapter nullAdapter = adapterFactory.matchAdapter(se);
    Assertions.assertNull(nullAdapter);

    PortOrderCreateAdapter adapter = new PortOrderCreateAdapter(new RouteAdapterContext(null));

    assertThrows(VortexException.class, () -> adapter.process(exchange, null));
  }
}
