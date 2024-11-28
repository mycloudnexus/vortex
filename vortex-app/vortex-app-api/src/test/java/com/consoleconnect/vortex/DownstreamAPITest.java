package com.consoleconnect.vortex;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("auth-hs256")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WireMockTest(httpPort = 3031)
class DownstreamAPITest extends AbstractIntegrationTest {

  private final TestUser mgmtUser;
  private final TestUser unInvitedMgmtUser;
  private final TestUser anonymousUser;
  private final TestUser customerUser;

  public DownstreamAPITest(@Autowired WebTestClient webTestClient) {
    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);

    this.mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    this.customerUser = TestUser.loginAsCustomerUser(webTestClientHelper);
    this.anonymousUser = TestUser.login(webTestClientHelper, null);
    this.unInvitedMgmtUser =
        TestUser.login(webTestClientHelper, AuthContextConstants.MGMT_ACCESS_TOKEN_2);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @BeforeEach
  void setUpEach() {
    MockServerHelper.setupMock("consoleconnect");
  }

  @Test
  void givenAnonymous_whenGetHeartbeat_thenReturn401() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenUninvitedMgmtUser_whenGetHeartbeat_thenReturn404() {
    unInvitedMgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);
  }

  @Test
  void givenMgmtAccessToken_whenGetHeartbeat_thenReturn200() {
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    MockServerHelper.verify(1, "/heartbeat", AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  void givenCustomerAccessToken_whenGetHeartbeat_thenReturn200() {
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // Verify that the request was made to the correct endpoint with the correct access token
    MockServerHelper.verify(1, "/heartbeat", AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  void givenMgmtAccessToken_whenGetCurrentUserInfo_thenReturn200() {
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/api/auth/token").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    MockServerHelper.verify(1, "/api/auth/token", AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  void givenUnInvitedMgmtAccessToken_whenGetCurrentUserInfo_thenReturn403() {
    unInvitedMgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/api/auth/token").build(),
        HttpStatus.FORBIDDEN.value(),
        Assertions::assertNull);
  }

  @Test
  void givenCustomerAccessToken_whenGetCurrentUserInfo_thenReturn200() {
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/api/auth/token").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // Verify that the request was made to the correct endpoint with the correct access token
    MockServerHelper.verify(1, "/api/auth/token", AuthContextConstants.CUSTOMER_API_KEY);
  }
}
