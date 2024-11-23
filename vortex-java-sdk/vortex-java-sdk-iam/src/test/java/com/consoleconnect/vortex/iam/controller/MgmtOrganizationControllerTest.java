package com.consoleconnect.vortex.iam.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.MemberInfoUpdateDto;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.toolkit.Auth0PageHelper;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ActiveProfiles("auth-hs256")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = TestApplication.class)
@WireMockTest(httpPort = 3032)
@Slf4j
class MgmtOrganizationControllerTest extends AbstractIntegrationTest {
  private OrganizationService organizationService = mock(OrganizationService.class);
  private MgmtOrganizationController mgmtOrganizationController =
      new MgmtOrganizationController(organizationService);

  private final TestUser mgmtUser;
  private final TestUser customerUser;
  private final TestUser anonymousUser;

  @Autowired
  public MgmtOrganizationControllerTest(WebTestClient webTestClient) {
    WebTestClientHelper webTestClientHelper = new WebTestClientHelper(webTestClient);

    this.mgmtUser = TestUser.loginAsMgmtUser(webTestClientHelper);
    this.customerUser = TestUser.loginAsCustomerUser(webTestClientHelper);
    this.anonymousUser = TestUser.login(webTestClientHelper, null);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @BeforeEach
  void setUpEach() {
    MockServerHelper.setupMock("auth0");
  }

  @Test
  void givenAnonymousUser_whenSearch_thenReturn401() {
    String endpoint = "/mgmt/organizations";

    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        401,
        org.junit.jupiter.api.Assertions::assertNull);
  }

  @Test
  void givenCustomerUser_whenSearch_thenReturn401() {
    String endpoint = "/mgmt/organizations";

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        403,
        org.junit.jupiter.api.Assertions::assertNull);
  }

  @Test
  void givenOrganizationCreated_whenSearch_thenReturn200() {
    String endpoint = "/mgmt/organizations";
    String auth0Endpoint = "/api/v2/organizations?per_page=%d&page=0&include_totals=true";

    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    String url = String.format(auth0Endpoint, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);

    // give size=-1 to load all data
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).queryParam("size", PagingHelper.ALL_STR).build(),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    url = String.format(auth0Endpoint, Auth0PageHelper.MAX_SIZE_PER_PAGE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMemberCreated_whenSearchMembers_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/members";
    String auth0Endpoint =
        "/api/v2/organizations/%s/members?per_page=%d&page=0&include_totals=true";
    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(AuthContextConstants.CUSTOMER_COMPANY_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    String url =
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);

    // given size=-1 to load all data
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(endpoint)
                .queryParam("size", PagingHelper.ALL_STR)
                .build(AuthContextConstants.CUSTOMER_COMPANY_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    url =
        String.format(
            auth0Endpoint,
            AuthContextConstants.CUSTOMER_COMPANY_ID,
            Auth0PageHelper.MAX_SIZE_PER_PAGE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenInvitationCreated_whenSearchInvitations_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/invitations";
    String auth0Endpoint =
        "/api/v2/organizations/%s/invitations?per_page=%d&page=0&include_totals=true";
    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(AuthContextConstants.CUSTOMER_COMPANY_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    String url =
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);

    // given size=-1 to load all data
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder
                .path(endpoint)
                .queryParam("size", PagingHelper.ALL_STR)
                .build(AuthContextConstants.CUSTOMER_COMPANY_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    url =
        String.format(
            auth0Endpoint,
            AuthContextConstants.CUSTOMER_COMPANY_ID,
            Auth0PageHelper.MAX_SIZE_PER_PAGE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void test_revokeInvitation() {
    Mono<HttpResponse<Void>> responseMono =
        mgmtOrganizationController.revokeInvitation(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  @Test
  void test_block() {
    doReturn(mock(User.class))
        .when(organizationService)
        .changeMemberStatus(anyString(), anyString(), anyBoolean(), any());
    Mono<HttpResponse<User>> responseMono =
        mgmtOrganizationController.changeMemberStatus(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            false,
            getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  @Test
  void test_resetPassword() {
    Mono<HttpResponse<Void>> responseMono =
        mgmtOrganizationController.resetPassword(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  @Test
  void test_updateMemberInfo() {
    doReturn(mock(User.class))
        .when(organizationService)
        .changeMemberStatus(anyString(), anyString(), anyBoolean(), any());
    MemberInfoUpdateDto memberInfoUpdateDto = new MemberInfoUpdateDto();
    memberInfoUpdateDto.setFamilyName("familyName");
    memberInfoUpdateDto.setGivenName("givenName");
    Mono<HttpResponse<User>> responseMono =
        mgmtOrganizationController.updateMemberInfo(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            memberInfoUpdateDto,
            getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  private JwtAuthenticationToken getAuthenticationToken() {
    Jwt jwt =
        Jwt.withTokenValue("token").subject("test").header("Authorization", "Bearer ").build();
    return new JwtAuthenticationToken(jwt);
  }
}
