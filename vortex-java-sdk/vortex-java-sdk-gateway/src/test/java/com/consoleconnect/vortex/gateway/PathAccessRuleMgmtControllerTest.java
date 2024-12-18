package com.consoleconnect.vortex.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.gateway.dto.CreatePathAccessRuleRequest;
import com.consoleconnect.vortex.gateway.dto.UpdatePathAccessRuleRequest;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
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
class PathAccessRuleMgmtControllerTest extends AbstractIntegrationTest {

  private final TestUser mgmtUser;
  private final TestUser customerUser;

  @Autowired
  public PathAccessRuleMgmtControllerTest(WebTestClient webTestClient) {

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
  void givenCustomerUser_whenCrudPathAccessRule_thenReturn403() {

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/path-access-rules").build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/mgmt/path-access-rules").build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder.path("/mgmt/path-access-rules/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder ->
            uriBuilder.path("/mgmt/path-access-rules/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
    customerUser.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder ->
            uriBuilder.path("/mgmt/path-access-rules/" + UUID.randomUUID().toString()).build(),
        403,
        Assertions::assertNull);
  }

  @Test
  @Order(2)
  void givenMgmtUser_whenCrudResource_thenReturn200() {

    // create
    CreatePathAccessRuleRequest createPathAccessRuleRequest = new CreatePathAccessRuleRequest();
    createPathAccessRuleRequest.setPath("/api/test/1");
    createPathAccessRuleRequest.setMethod("GET");
    createPathAccessRuleRequest.setAction(AccessActionEnum.ALLOWED);
    Optional<String> resOptional =
        mgmtUser.requestAndVerify(
            HttpMethod.POST,
            uriBuilder -> uriBuilder.path("/mgmt/path-access-rules").build(),
            createPathAccessRuleRequest,
            200,
            Assertions::assertNotNull);
    Assertions.assertFalse(resOptional.isEmpty());

    String id = JsonPathToolkit.read(resOptional.get(), "$.data.id");
    Assertions.assertNotNull(id);
    Assertions.assertEquals("/api/test/1", JsonPathToolkit.read(resOptional.get(), "$.data.path"));
    Assertions.assertEquals("GET", JsonPathToolkit.read(resOptional.get(), "$.data.method"));
    Assertions.assertEquals("ALLOWED", JsonPathToolkit.read(resOptional.get(), "$.data.action"));

    // retrieve by id
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/path-access-rules/" + id).build(),
        200,
        Assertions::assertNotNull);

    // update by id
    UpdatePathAccessRuleRequest updateResourceRequest = new UpdatePathAccessRuleRequest();
    updateResourceRequest.setPath("/api/test/2");
    updateResourceRequest.setMethod("POST");
    updateResourceRequest.setAction(AccessActionEnum.DENIED);

    Optional<String> updatedRes =
        mgmtUser.requestAndVerify(
            HttpMethod.PATCH,
            uriBuilder -> uriBuilder.path("/mgmt/path-access-rules/" + id).build(),
            updateResourceRequest,
            200,
            Assertions::assertNotNull);

    Assertions.assertFalse(updatedRes.isEmpty());

    Assertions.assertEquals("/api/test/2", JsonPathToolkit.read(updatedRes.get(), "$.data.path"));
    Assertions.assertEquals("POST", JsonPathToolkit.read(updatedRes.get(), "$.data.method"));
    Assertions.assertEquals("DENIED", JsonPathToolkit.read(updatedRes.get(), "$.data.action"));

    // delete by id
    mgmtUser.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path("/mgmt/path-access-rules/" + id).build(),
        200,
        Assertions::assertNotNull);

    // retrieve by id
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/path-access-rules/" + id).build(),
        404,
        Assertions::assertNotNull);
  }
}
