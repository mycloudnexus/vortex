package com.consoleconnect.vortex.gateway.adapter;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.adapter.cc.DefaultResourceOrderCreateAdapter;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.OrderRepository;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.FixedLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@MockIntegrationTest
class ResponseAdapterTest extends AbstractIntegrationTest {

  @Autowired private RouteAdapterFactory adapterFactory;
  @Autowired private OrderService orderService;

  @SpyBean private OrderRepository orderRepository;

  private UserContext userContext;
  private ServerWebExchange exchange;
  private MockServerHttpResponse response;

  @BeforeEach
  void setUp() {
    userContext = new UserContext();
    userContext.setCustomerId("orgId");

    response = new MockServerHttpResponse();
    response.setStatusCode(HttpStatus.OK);
  }

  @Test
  @Order(1)
  void testCreateAdapter() {
    MockServerHttpRequest request = MockServerHttpRequest.put("/test/api/do").build();
    exchange =
        new DefaultServerWebExchange(
            request,
            response,
            new DefaultWebSessionManager(),
            new DefaultServerCodecConfigurer(),
            new FixedLocaleContextResolver());

    exchange.getAttributes().put(IamConstants.X_VORTEX_USER_CONTEXT, userContext);
    RouteAdapter adapter = adapterFactory.matchAdapter(exchange);
    byte[] resBytes = "{\"id\":\"orderId\"}".getBytes();
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
    se.getAttributes().put(IamConstants.X_VORTEX_USER_CONTEXT, userContext);

    RouteAdapter nullAdapter = adapterFactory.matchAdapter(se);
    Assertions.assertNull(nullAdapter);

    DefaultResourceOrderCreateAdapter adapter =
        new DefaultResourceOrderCreateAdapter(
            new RouteAdapterContext(ResourceTypeEnum.PORT, orderService));

    assertThrows(VortexException.class, () -> adapter.process(exchange, null));
  }
}
