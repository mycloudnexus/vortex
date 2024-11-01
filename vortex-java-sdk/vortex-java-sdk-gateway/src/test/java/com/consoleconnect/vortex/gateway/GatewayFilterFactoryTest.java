package com.consoleconnect.vortex.gateway;

import static org.mockito.ArgumentMatchers.any;

import com.consoleconnect.vortex.gateway.adapter.RouteAdapter;
import com.consoleconnect.vortex.gateway.adapter.RouteAdapterFactory;
import com.consoleconnect.vortex.gateway.filter.ResponseAdapterGatewayFilterFactory;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
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

  @Autowired private RouteAdapterFactory adapterFactory;

  private ServerWebExchange exchange;
  private AbstractGatewayFilterFactory.NameConfig config =
      new AbstractGatewayFilterFactory.NameConfig();
  private ResponseAdapterGatewayFilterFactory filterFactory;
  private GatewayFilterChain chain;

  @BeforeEach
  void setUp() {
    config.setName("name");
    filterFactory = new ResponseAdapterGatewayFilterFactory(adapterFactory);

    MockServerHttpRequest request =
        MockServerHttpRequest.put(
                "/consoleconnect/api/company/consolecore-poping-company/ports/orders")
            .header("Authorization", "Bearer token")
            .build();

    MockServerHttpResponse response = new MockServerHttpResponse();
    response.setStatusCode(HttpStatus.OK);

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
              byte[] bytes = "{\"id\":\"id00100100\"}".getBytes(StandardCharsets.UTF_8);
              DefaultDataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
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
}
