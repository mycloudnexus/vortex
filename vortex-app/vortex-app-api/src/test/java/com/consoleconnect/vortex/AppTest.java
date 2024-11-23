package com.consoleconnect.vortex;

import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import com.consoleconnect.vortex.test.WebTestClientHelper;
import com.consoleconnect.vortex.test.user.TestUser;
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

  private final TestUser mgmtUser;
  private final TestUser anonymousUser;

  public AppTest(@Autowired WebTestClient webTestClient) {
    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);

    this.mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    this.anonymousUser = TestUser.login(webTestClientHelper, null);
  }

  @Test
  void givenAnonymous_whenGetHome_thenReturnRedirect() {

    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/").build(),
        HttpStatus.PERMANENT_REDIRECT.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAnonymous_whenGetSwaggerUi_thenReturn200() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/swagger-ui.html").build(),
        HttpStatus.FOUND.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAnonymous_whenGetHealth_thenReturn200() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/health").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetActuator_thenReturn401() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetActuator_thenReturn200() {
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetInfo_thenReturn401() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/info").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetInfo_thenReturn200() {
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/info").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }

  @Test
  void givenAnonymous_whenGetPrometheus_thenReturn401() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/prometheus").build(),
        HttpStatus.UNAUTHORIZED.value(),
        Assertions::assertNull);
  }

  @Test
  void givenAccessToken_whenGetPrometheus_thenReturn200() {
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/actuator/prometheus").build(),
        HttpStatus.OK.value(),
        Assertions::assertNotNull);
  }
}
