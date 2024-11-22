package com.consoleconnect.vortex;

import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.AuthContextConstants;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import com.consoleconnect.vortex.test.WebTestClientHelper;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("auth-hs256")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureObservability
class AppTest extends AbstractIntegrationTest {

  private final WebTestClientHelper webTestClient;

  public AppTest(@Autowired WebTestClient webTestClient) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
  }

  @Test
  void givenAnonymous_whenGetHome_thenReturnRedirect() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/").build(),
        HttpStatus.PERMANENT_REDIRECT.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAnonymous_whenGetSwaggerUi_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/swagger-ui.html").build(),
        HttpStatus.FOUND.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAnonymous_whenGetOpenAPISpec_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/v3/api-docs").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetDownstreamOpenAPISpec_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/v3/api-docs/downstream").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetHealth_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/health").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetActuator_thenReturn401() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetActuator_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetInfo_thenReturn401() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/info").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetInfo_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/info").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetPrometheus_thenReturn401() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/prometheus").build(),
        Map.of("accept", "text/plain;version=0.0.4;charset=utf-8"),
        null,
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetPrometheus_thenReturn200() {
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/prometheus").build(),
        Map.of(
            "accept",
            "text/plain;version=0.0.4;charset=utf-8",
            "Authorization",
            "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }
}
