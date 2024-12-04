package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import com.consoleconnect.vortex.test.WebTestClientHelper;
import com.consoleconnect.vortex.test.user.TestUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
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
class MgmtRoleControllerTest extends AbstractIntegrationTest {

  private final TestUser mgmtUser;
  private final TestUser anonymousUser;
  private final TestUser customerUser;

  public MgmtRoleControllerTest(@Autowired WebTestClient webTestClient) {
    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);

    mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    customerUser = TestUser.loginAsCustomerUser(webTestClientHelper);
    anonymousUser = TestUser.login(webTestClientHelper, null);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @Test
  void givenAnonymousUser_whenListRoles_thenReturn401() {

    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/roles").build(),
        401,
        Assertions::assertNull);
  }

  @Test
  void givenCustomerUser_whenListRoles_thenReturn403() {

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/roles").build(),
        403,
        Assertions::assertNull);
  }

  @Test
  void givenMgmtAccessToken_whenListRoles_thenReturn200() {

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/roles").build(),
        200,
        response -> {
          HttpResponse<List<RoleEnum>> res =
              JsonToolkit.fromJson(response, new TypeReference<>() {});
          log.info("{}", res);
          Assertions.assertEquals(2, res.getData().size());
          Assertions.assertTrue(res.getData().contains(RoleEnum.PLATFORM_ADMIN));
          Assertions.assertTrue(res.getData().contains(RoleEnum.PLATFORM_MEMBER));
        });
  }
}
