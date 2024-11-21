package com.consoleconnect.vortex;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.test.*;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Map;
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

  private final WebTestClientHelper webTestClient;

  public DownstreamAPITest(@Autowired WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
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
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenMgmtAccessToken_whenGetHeartbeat_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    MockServerHelper.verify(1, "/heartbeat", AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  void givenCustomerAccessToken_whenGetHeartbeat_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/heartbeat").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // Verify that the request was made to the correct endpoint with the correct access token
    MockServerHelper.verify(1, "/heartbeat", AuthContextConstants.CUSTOMER_API_KEY);
  }

  @Test
  void givenMgmtAccessToken_whenGetCurrentUserInfo_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/api/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    MockServerHelper.verify(1, "/api/auth/token", AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  void givenCustomerAccessToken_whenGetCurrentUserInfo_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/downstream/api/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);

    // Verify that the request was made to the correct endpoint with the correct access token
    MockServerHelper.verify(1, "/api/auth/token", AuthContextConstants.CUSTOMER_API_KEY);
  }
}
