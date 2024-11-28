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
import com.consoleconnect.vortex.test.user.TestUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
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

  @MockBean private MemberService memberService;

  private final TestUser mgmtUser;
  private final TestUser anonymousUser;
  private final TestUser customerUser;

  public AuthTokenControllerTest(@Autowired WebTestClient webTestClient) {
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

  @BeforeEach
  void setUpEach() {
    MockServerHelper.setupMock("consoleconnect");
  }

  @Test
  void givenAnonymousUser_whenGetAuthToken_thenReturn401() {
    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        401,
        Assertions::assertNull);
  }

  @Test
  void givenMgmtUser_whenGetAuthToken_thenReturnUserInfo() {

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
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
  void givenCustomerUser_whenGetAuthToken_thenReturnUserInfo() {

    MemberInfo memberInfo = new MemberInfo();
    memberInfo.setId(AuthContextConstants.CUSTOMER_USER_ID);
    memberInfo.setOrganization(null);
    memberInfo.setName("first last");
    Mockito.doReturn(memberInfo).when(memberService).getUserInfo(Mockito.anyString());

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        200,
        response -> {
          HttpResponse<AuthToken> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          log.info("{}", res);
          Assertions.assertEquals(AuthContextConstants.CUSTOMER_USER_ID, res.getData().getUserId());
        });
  }
}
