package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.AuthToken;
import com.consoleconnect.vortex.iam.dto.MemberInfo;
import com.consoleconnect.vortex.iam.service.MemberService;
import com.consoleconnect.vortex.test.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class AuthTokenControllerTest extends AbstractIntegrationTest {

  private final WebTestClientHelper webTestClient;

  @MockBean private MemberService memberService;

  public AuthTokenControllerTest(@Autowired WebTestClient webTestClient) {
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
  void givenNoAccessToken_whenGetAuthToken_thenReturn401() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        401,
        Assertions::assertNull);
  }

  @Test
  void givenMgmtAccessToken_whenGetAuthToken_thenReturnUserInfo() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        200,
        response -> {
          HttpResponse<AuthToken> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertEquals(AuthContextConstants.MGMT_USER_ID, res.getData().getUserId());
        });

    MockServerHelper.verify(
        1,
        String.format("/v2/companies/%s/members?pageSize=0", AuthContextConstants.MGMT_COMPANY_ID),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
    MockServerHelper.verify(
        1,
        String.format("/api/user/%s", AuthContextConstants.MGMT_USERNAME),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  void givenCustomerAccessToken_whenGetAuthToken_thenReturnUserInfo() {

    MemberInfo memberInfo = new MemberInfo();
    memberInfo.setId(AuthContextConstants.CUSTOMER_USER_ID);
    memberInfo.setOrganization(null);
    memberInfo.setName("first last");
    Mockito.doReturn(memberInfo).when(memberService).getUserInfo(Mockito.anyString());

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        response -> {
          HttpResponse<AuthToken> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          log.info("{}", res);
          Assertions.assertEquals(AuthContextConstants.CUSTOMER_USER_ID, res.getData().getUserId());
        });
  }
}
