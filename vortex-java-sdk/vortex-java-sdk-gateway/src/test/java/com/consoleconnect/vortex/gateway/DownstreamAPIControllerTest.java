package com.consoleconnect.vortex.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.data.ListResponse;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.ResourceRepository;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.dto.OrganizationInfo;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
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

  @SpyBean private OrganizationService organizationService;

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
        Assertions::assertNotNull);

    MockServerHelper.verify(1, "/auth/token", AuthContextConstants.CUSTOMER_API_KEY);
  }

  void initializeOrderPorts() {
    // create a resource for the customer company
    CreateResourceRequest createResourceRequest = new CreateResourceRequest();
    createResourceRequest.setCustomerId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    createResourceRequest.setResourceType(ResourceTypeEnum.ORDER_PORT.name());
    createResourceRequest.setOrderId("66e28efa04a4727d5387da10");
    resourceService.create(createResourceRequest, null);

    // create a resource for the mgmt company
    createResourceRequest.setCustomerId(AuthContextConstants.MGMT_COMPANY_ID);
    createResourceRequest.setOrderId("66e28efa04a4727d5387da11");
    resourceService.create(createResourceRequest, null);
  }

  void createPorts() {
    // create a resource for the customer company
    resourceService.updateResourceId(
        AuthContextConstants.CUSTOMER_COMPANY_ID,
        ResourceTypeEnum.ORDER_PORT.name(),
        "66e28efa04a4727d5387da10",
        "5762451a82894b9b1b1a5dc2");

    // create a resource for the mgmt company
    resourceService.updateResourceId(
        AuthContextConstants.MGMT_COMPANY_ID,
        ResourceTypeEnum.ORDER_PORT.name(),
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
                  AuthContextConstants.CUSTOMER_COMPANY_ID, ResourceTypeEnum.ORDER_PORT.name());

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
                  AuthContextConstants.MGMT_COMPANY_ID, ResourceTypeEnum.ORDER_PORT.name());

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
  void givenOrderCreated_whenCustomerUserListOrder_thenOnlyListOrderAssigned() {

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

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "66e28efa04a4727d5387da10",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
          Assertions.assertNull(
              JsonPathToolkit.read(
                  JsonToolkit.toJson(listResponse.getResults().get(0)), "$.createdPortId"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  @Order(3)
  void givenOrderCreated_whenMgmtUserListOrder_thenOnlyListOrderAssigned() {

    initializeOrderPorts();

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME);

    // mgmt user should see its own order
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);

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
  @Order(3)
  void givenOrderCreatedAndPortCreated_whenCustomerUserListOrder_thenResourceIdSynced() {

    initializeOrderPorts();

    MockServerHelper.setupMock("consoleconnect/order-with-createdPortId");

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

          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "66e28efa04a4727d5387da10", JsonPathToolkit.read(res, "$.results[0].id"));
          Assertions.assertEquals(
              "port-02", JsonPathToolkit.read(res, "$.results[0].createdPortId"));

          resourceRepository
              .findOneByCustomerIdAndResourceTypeAndOrderId(
                  AuthContextConstants.CUSTOMER_COMPANY_ID,
                  ResourceTypeEnum.ORDER_PORT.name(),
                  "66e28efa04a4727d5387da10")
              .ifPresentOrElse(
                  resourceEntity -> {
                    Assertions.assertEquals("port-02", resourceEntity.getResourceId());
                  },
                  () -> Assertions.fail("Resource not found"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  @Order(3)
  void givenOrderCreatedAndPortCreated_whenMgmtUserListOrder_thenResourceIdSynced() {

    initializeOrderPorts();

    MockServerHelper.setupMock("consoleconnect/order-with-createdPortId");

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/orders", AuthContextConstants.MGMT_COMPANY_USERNAME);

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          log.info("Response:{}", res);
          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "66e28efa04a4727d5387da11", JsonPathToolkit.read(res, "$.results[0].id"));
          Assertions.assertEquals(
              "port-01", JsonPathToolkit.read(res, "$.results[0].createdPortId"));

          resourceRepository
              .findOneByCustomerIdAndResourceTypeAndOrderId(
                  AuthContextConstants.MGMT_COMPANY_ID,
                  ResourceTypeEnum.ORDER_PORT.name(),
                  "66e28efa04a4727d5387da11")
              .ifPresentOrElse(
                  resourceEntity -> {
                    Assertions.assertEquals("port-01", resourceEntity.getResourceId());
                  },
                  () -> Assertions.fail("Resource not found"));
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
  void givenPortCreated_whenCustomerUserListPorts_thenOnlyListPortAssigned() {

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

    createPorts();

    // customer should not see the mgmt order
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          ListResponse listResponse = JsonToolkit.fromJson(res, ListResponse.class);
          Assertions.assertNotNull(listResponse);
          Assertions.assertEquals(1, listResponse.getResults().size());
          Assertions.assertEquals(
              "5762451a82894b9b1b1a5dc2",
              JsonPathToolkit.read(JsonToolkit.toJson(listResponse.getResults().get(0)), "$.id"));
        });

    // access downstream api via mgmt access token
    MockServerHelper.verify(
        2,
        HttpMethod.GET,
        String.format("/api/company/%s/ports", AuthContextConstants.MGMT_COMPANY_USERNAME),
        AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  @Order(4)
  void givenPortCreated_whenMgmtUserListPorts_thenOnlyListPortAssigned() {

    initializeOrderPorts();

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports", AuthContextConstants.MGMT_COMPANY_USERNAME);

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

    // mgmt user should see its own order
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);

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
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  @Order(4)
  void
      givenConnectionCreated_whenCustomerUserListPorConnections_thenConnectionDestCompanyNameChanged() {

    OrganizationInfo org = new OrganizationInfo();
    org.setId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    org.setName("Customer Company");
    Mockito.doReturn(org).when(organizationService).findOne(Mockito.anyString());

    String orderId = UUID.randomUUID().toString();
    String portId = UUID.randomUUID().toString();
    // create a order and set the portId
    CreateResourceRequest request = new CreateResourceRequest();
    request.setCustomerId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    request.setResourceType(ResourceTypeEnum.ORDER_PORT.name());
    request.setResourceId(portId);
    request.setOrderId(orderId);
    resourceService.create(request, null);

    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/%s/connections",
            AuthContextConstants.MGMT_COMPANY_USERNAME, portId);

    // companyName should be changed to Customer Company
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          Assertions.assertEquals(
              org.getName(), JsonPathToolkit.read(res, "$.results[0].destCompany.name"));
          Assertions.assertEquals(
              org.getName(),
              JsonPathToolkit.read(res, "$.results[0].destCompany.company.registeredName"));
        });

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format(
            "/api/company/%s/ports/%s/connections",
            AuthContextConstants.MGMT_COMPANY_USERNAME, portId),
        AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  @Order(4)
  void
      givenConnectionCreated_whenMgmtUserListPorConnections_thenConnectionDestCompanyNameNotChanged() {

    OrganizationInfo org = new OrganizationInfo();
    org.setId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    org.setName("Customer Company");
    Mockito.doReturn(org).when(organizationService).findOne(Mockito.anyString());

    String companyName = "dest company name";
    String portId = UUID.randomUUID().toString();
    String endpoint =
        String.format(
            "/downstream/api/company/%s/ports/%s/connections",
            AuthContextConstants.MGMT_COMPANY_USERNAME, portId);

    String orderId = UUID.randomUUID().toString();
    // create a order and set the portId
    CreateResourceRequest request = new CreateResourceRequest();
    request.setCustomerId(AuthContextConstants.CUSTOMER_COMPANY_ID);
    request.setResourceType(ResourceTypeEnum.ORDER_PORT.name());
    request.setResourceId(portId);
    request.setOrderId(orderId);
    resourceService.create(request, null);

    // companyName should be changed to Customer Company
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        res -> {
          Assertions.assertNotNull(res);
          Assertions.assertEquals(
              companyName, JsonPathToolkit.read(res, "$.results[0].destCompany.name"));
          Assertions.assertEquals(
              companyName,
              JsonPathToolkit.read(res, "$.results[0].destCompany.company.registeredName"));
        });

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format(
            "/api/company/%s/ports/%s/connections",
            AuthContextConstants.MGMT_COMPANY_USERNAME, portId),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }
}
