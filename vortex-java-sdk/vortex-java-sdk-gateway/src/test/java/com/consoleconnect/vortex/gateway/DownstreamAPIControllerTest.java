package com.consoleconnect.vortex.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.ResourceRepository;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("auth-hs256")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = TestApplication.class)
@WireMockTest(httpPort = 3031)
@Slf4j
class DownstreamAPIControllerTest extends AbstractIntegrationTest {

  private final TestUser mgmtUser;
  private final TestUser customerUser;

  @Autowired private ResourceService resourceService;
  @Autowired private ResourceRepository resourceRepository;

  @Autowired
  public DownstreamAPIControllerTest(WebTestClient webTestClient) {

    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);
    mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    customerUser = TestUser.loginAsCustomerUser(webTestClientHelper);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @BeforeEach
  void setUpEach() {
    MockServerHelper.setupMock("consoleconnect");
    resourceRepository.deleteAll();
  }

  @Test
  @Order(1)
  void givenCustomerUser_whenGetUserInfo_thenReturn200() {

    String endpoint = "/downstream/auth/token";
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          System.out.println(res);
          Assertions.assertNotNull(res);
        });

    MockServerHelper.verify(1, "/auth/token", AuthContextConstants.CUSTOMER_API_KEY);
  }

  void initializeOrderPorts() {
    // create a resource for the customer company
    CreateResourceRequest createResourceRequest = new CreateResourceRequest();
    createResourceRequest.setCustomerId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    createResourceRequest.setResourceType(ResourceTypeEnum.ORDER_PORT);
    createResourceRequest.setOrderId("66e28efa04a4727d5387da10");
    resourceService.create(createResourceRequest);

    // create a resource for the mgmt company
    createResourceRequest.setCustomerId(AuthContextConstants.MGMT_COMPANY_ID);
    createResourceRequest.setOrderId("66e28efa04a4727d5387da11");
    resourceService.create(createResourceRequest);
  }

  void createPorts() {
    // create a resource for the customer company
    resourceService.updateResourceId(
        AuthContextConstants.CUSTOMER_COMPANY_ID,
        ResourceTypeEnum.ORDER_PORT,
        "66e28efa04a4727d5387da10",
        "5762451a82894b9b1b1a5dc2");

    // create a resource for the mgmt company
    resourceService.updateResourceId(
        AuthContextConstants.MGMT_COMPANY_ID,
        ResourceTypeEnum.ORDER_PORT,
        "66e28efa04a4727d5387da11",
        "5762451a82894b9b1b1a5dc3");
  }

  @Test
  @Order(1)
  void givenMgmtUser_whenOrderPortForCustomer_thenReturn200() {

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME);

    Map<String, Object> requestPayload =
        Map.of(
            "dataCenterFacility",
            "demodatacenter",
            "speed",
            1000,
            "portName",
            "Demo Port",
            "paymentType",
            "invoice",
            "durationUnit",
            "y");

    mgmtUser.requestAndVerify(
        HttpMethod.PUT,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of(IamConstants.X_VORTEX_CUSTOMER_ID, AuthContextConstants.CUSTOMER_COMPANY_ID),
        requestPayload,
        200,
        res -> {
          Assertions.assertNotNull(res);

          String orderId = JsonPathToolkit.read(res, "$.id");
          Assertions.assertNotNull(orderId);

          // the resource should be associated with the customer company
          List<ResourceEntity> resourceEntityList =
              resourceService.findAllByCustomerIdAndResourceType(
                  AuthContextConstants.CUSTOMER_COMPANY_ID, ResourceTypeEnum.ORDER_PORT);

          Assertions.assertNotNull(resourceEntityList);
          Assertions.assertEquals(1, resourceEntityList.size());
          Assertions.assertEquals(orderId, resourceEntityList.get(0).getOrderId());
          Assertions.assertEquals(
              AuthContextConstants.CUSTOMER_COMPANY_ID, resourceEntityList.get(0).getCustomerId());
          Assertions.assertNull(resourceEntityList.get(0).getResourceId());
        });

    MockServerHelper.verify(
        1,
        HttpMethod.PUT,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  @Order(2)
  void givenMgmtUser_whenOrderPortForItself_thenReturn200() {

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME);

    Map<String, Object> requestPayload =
        Map.of(
            "dataCenterFacility",
            "demodatacenter",
            "speed",
            1000,
            "portName",
            "Demo Port",
            "paymentType",
            "invoice",
            "durationUnit",
            "y");

    mgmtUser.requestAndVerify(
        HttpMethod.PUT,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        requestPayload,
        200,
        res -> {
          Assertions.assertNotNull(res);

          String orderId = JsonPathToolkit.read(res, "$.id");
          Assertions.assertNotNull(orderId);

          // the resource should be associated with the mgmt company
          List<ResourceEntity> resourceEntityList =
              resourceService.findAllByCustomerIdAndResourceType(
                  AuthContextConstants.MGMT_COMPANY_ID, ResourceTypeEnum.ORDER_PORT);

          Assertions.assertNotNull(resourceEntityList);
          Assertions.assertEquals(1, resourceEntityList.size());
          Assertions.assertEquals(orderId, resourceEntityList.get(0).getOrderId());
          Assertions.assertEquals(
              AuthContextConstants.MGMT_COMPANY_ID, resourceEntityList.get(0).getCustomerId());
          Assertions.assertNull(resourceEntityList.get(0).getResourceId());
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        1,
        HttpMethod.PUT,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  @Order(3)
  void givenOrderCreated_whenListOrder_thenOnlyListOrderAssigned() {

    initializeOrderPorts();

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME);

    // customer should not see the mgmt order
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          System.out.println(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "66e28efa04a4727d5387da10",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.CUSTOMER_API_KEY);

    // mgmt user should see its own order
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          System.out.println(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "66e28efa04a4727d5387da11",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  @Order(4)
  void givenPortCreated_whenListPorts_thenOnlyListPortAssigned() {

    initializeOrderPorts();

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports", AuthContextConstants.MGMT_COMPANY_USERNAME);

    // customer should not see any ports until the resource is created
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(0, listResponse.getResults().size());
        });

    // mgmt should not see any ports until the resource is created
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(0, listResponse.getResults().size());
        });

    createPorts();

    // customer should not see the mgmt order
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          System.out.println(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "5762451a82894b9b1b1a5dc2",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
        });

    // mgmt user should see its own order
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          System.out.println(res);

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "5762451a82894b9b1b1a5dc3",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        2,
        HttpMethod.GET,
        String.format("/api/company/%s/ports", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.CUSTOMER_API_KEY);

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        2,
        HttpMethod.GET,
        String.format("/api/company/%s/ports", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Data
  public static class ListResponse {
    private List<Object> results;
  }
}
