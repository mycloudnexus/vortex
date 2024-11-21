package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.OrderRepository;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.FixedLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@MockIntegrationTest
@ActiveProfiles("auth-hs256")
class ResponseTransformerTest extends AbstractIntegrationTest {

  @Autowired private DefaultCreateResourceOrderTransformer orderTransformer;
  @Autowired private DefaultResourceListTransformer listTransformer;
  @Autowired private PortOrderListTransformer portOrderListTransformer;
  @Autowired private OrderService orderService;

  @SpyBean private OrderRepository orderRepository;

  private ServerWebExchange exchange;
  private MockServerHttpResponse response;

  private final String customerId = UUID.randomUUID().toString();
  private final String accessToken = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {

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

    exchange.getAttributes().put(IamConstants.X_VORTEX_CUSTOMER_ID, customerId);
    exchange.getAttributes().put(IamConstants.X_VORTEX_ACCESS_TOKEN, accessToken);

    TransformerApiProperty config = new TransformerApiProperty();
    config.setHttpMethod(HttpMethod.PUT);
    config.setHttpPath("/test/api/do");
    config.setTransformer("resource.create");
    config.setResourceType(ResourceTypeEnum.PORT);

    orderTransformer.getTransformerId();
    byte[] resBytes = "{\"id\":\"orderId\"}".getBytes();
    byte[] ret = orderTransformer.doTransform(exchange, resBytes, customerId, config);
    Assertions.assertNotNull(ret);
  }

  @Test
  @Order(2)
  void testListPortOrdersAdapter() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get(
                "/consoleconnect/api/company/consolecore-poping-company/ports/orders")
            .build();
    exchange =
        new DefaultServerWebExchange(
            request,
            response,
            new DefaultWebSessionManager(),
            new DefaultServerCodecConfigurer(),
            new FixedLocaleContextResolver());
    exchange.getAttributes().put(IamConstants.X_VORTEX_CUSTOMER_ID, customerId);
    exchange.getAttributes().put(IamConstants.X_VORTEX_ACCESS_TOKEN, accessToken);

    TransformerApiProperty config = new TransformerApiProperty();
    config.setHttpMethod(HttpMethod.GET);
    config.setHttpPath("/test/api/do");
    config.setTransformer("port.order.list");
    config.setResourceType(ResourceTypeEnum.PORT);
    config.setResponseBodyPath("$.results");

    byte[] resBytes =
        "{\"results\": [{\"id\":\"orderId\", \"createdPortId\":\"portId\"},{\"id\":\"orderId2\"}]}"
            .getBytes();
    orderTransformer.getTransformerId();
    byte[] ret = portOrderListTransformer.doTransform(exchange, resBytes, customerId, config);
    Assertions.assertNotNull(ret);
  }

  @Test
  @Order(3)
  void testDefaultResourceListAdapter() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/test/api/do").build();

    exchange =
        new DefaultServerWebExchange(
            request,
            response,
            new DefaultWebSessionManager(),
            new DefaultServerCodecConfigurer(),
            new FixedLocaleContextResolver());

    exchange.getAttributes().put(IamConstants.X_VORTEX_CUSTOMER_ID, customerId);
    exchange.getAttributes().put(IamConstants.X_VORTEX_ACCESS_TOKEN, accessToken);

    TransformerApiProperty config = new TransformerApiProperty();
    config.setHttpMethod(HttpMethod.GET);
    config.setHttpPath("/test/api/do");
    config.setTransformer("resource.list");
    config.setResourceType(ResourceTypeEnum.PORT);
    config.setResponseBodyPath("$.results");

    byte[] resBytes =
        "{\"results\": [{\"id\":\"portId\", \"status\":\"ACTIVE\"}, {\"id\":\"portId2\", \"status\":\"ACTIVE\"}]}"
            .getBytes();
    byte[] ret = listTransformer.doTransform(exchange, resBytes, customerId, config);
    Assertions.assertNotNull(ret);
  }
}
