package com.consoleconnect.vortex.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.dto.UpdateResourceRequest;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Optional;
import java.util.UUID;
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
class ResourceMgmtControllerTest extends AbstractIntegrationTest {

  private final TestUser mgmtUser;
  private final TestUser customerUser;

  @Autowired
  public ResourceMgmtControllerTest(WebTestClient webTestClient) {

    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);
    mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    customerUser = TestUser.loginAsCustomerUser(webTestClientHelper);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @Test
  @Order(1)
  void givenCustomerUser_whenCrudResource_thenReturn403() {

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/resources").build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/mgmt/resources").build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
  }

  @Test
  @Order(2)
  void givenMgmtUser_whenCrudResource_thenReturn200() {

    // create
    CreateResourceRequest createResourceRequest = new CreateResourceRequest();
    createResourceRequest.setResourceId("resource_1");
    createResourceRequest.setCustomerId("customer_1");
    createResourceRequest.setResourceType("type_1");
    createResourceRequest.setOrderId("order_1");
    Optional<String> resOptional =
        mgmtUser.requestAndVerify(
            HttpMethod.POST,
            uriBuilder -> uriBuilder.path("/mgmt/resources").build(),
            createResourceRequest,
            200,
            Assertions::assertNotNull);
    Assertions.assertFalse(resOptional.isEmpty());

    String id = JsonPathToolkit.read(resOptional.get(), "$.data.id");
    Assertions.assertNotNull(id);
    Assertions.assertEquals(
        "resource_1", JsonPathToolkit.read(resOptional.get(), "$.data.resourceId"));
    Assertions.assertEquals(
        "customer_1", JsonPathToolkit.read(resOptional.get(), "$.data.customerId"));
    Assertions.assertEquals(
        "type_1", JsonPathToolkit.read(resOptional.get(), "$.data.resourceType"));
    Assertions.assertEquals("order_1", JsonPathToolkit.read(resOptional.get(), "$.data.orderId"));

    // retrieve by id
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + id).build(),
        200,
        Assertions::assertNotNull);

    // update by id
    UpdateResourceRequest updateResourceRequest = new UpdateResourceRequest();
    updateResourceRequest.setResourceId("resource_2");
    updateResourceRequest.setCustomerId("customer_2");
    updateResourceRequest.setResourceType("type_2");
    updateResourceRequest.setOrderId("order_2");

    Optional<String> updatedRes =
        mgmtUser.requestAndVerify(
            HttpMethod.PATCH,
            uriBuilder -> uriBuilder.path("/mgmt/resources/" + id).build(),
            updateResourceRequest,
            200,
            Assertions::assertNotNull);

    Assertions.assertFalse(updatedRes.isEmpty());

    Assertions.assertEquals(
        "resource_2", JsonPathToolkit.read(updatedRes.get(), "$.data.resourceId"));
    Assertions.assertEquals(
        "customer_2", JsonPathToolkit.read(updatedRes.get(), "$.data.customerId"));
    Assertions.assertEquals(
        "type_2", JsonPathToolkit.read(updatedRes.get(), "$.data.resourceType"));
    Assertions.assertEquals("order_2", JsonPathToolkit.read(updatedRes.get(), "$.data.orderId"));

    // delete by id
    mgmtUser.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + id).build(),
        200,
        Assertions::assertNotNull);

    // retrieve by id
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/resources/" + id).build(),
        404,
        Assertions::assertNotNull);
  }
}
