package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Endpoint;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.dto.OidcConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.toolkit.Auth0PageHelper;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
@WireMockTest(httpPort = 3032)
@Slf4j
class MgmtOrganizationControllerTest extends AbstractIntegrationTest {

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
  void givenCustomerUser_whenCreateOrganization_thenReturn403() {
    String endpoint = "/mgmt/organizations";

    CreateOrganizationDto request = new CreateOrganizationDto();
    request.setName("test");
    request.setDisplayName("test");

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        request,
        403,
        org.junit.jupiter.api.Assertions::assertNull);
  }

  @Test
  void givenMgmtUser_whenCreateOrganizationWithWrongParameters_thenReturn400() {
    String endpoint = "/mgmt/organizations";

    // given empty request
    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        null,
        400,
        org.junit.jupiter.api.Assertions::assertNotNull);

    // name over 25 characters
    CreateOrganizationDto request = new CreateOrganizationDto();
    request.setName(RandomStringUtils.random(21, true, false));
    request.setDisplayName(UUID.randomUUID().toString());
    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        request,
        400,
        res -> log.info("{}", res));

    // display over 255 characters
    request = new CreateOrganizationDto();
    request.setName("test");
    request.setDisplayName(RandomStringUtils.random(256, true, false));
    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        request,
        400,
        res -> log.info("{}", res));
  }

  @Test
  void givenMgmtUser_whenCreateOrganization_thenReturn200() {
    String endpoint = "/mgmt/organizations";
    String auth0Endpoint = "/api/v2/organizations";

    CreateOrganizationDto request = new CreateOrganizationDto();
    request.setName("test");
    request.setDisplayName("test");

    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    String url = String.format(auth0Endpoint, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, HttpMethod.POST, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenOrganizationCreated_thenCreateOidcConnection_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/connection";

    String orgId = "org_0bcbzk1UJV9CvwAU";
    String connectionId = "con_YNEZH8rgZ8sQz9Fq";
    String invitationId = "uinv_WuhQogrsLDMF8L8y";

    CreateConnectionDto request = new CreateConnectionDto();
    request.setStrategy(ConnectionStrategyEnum.OIDC);
    OidcConnectionDto oidc = new OidcConnectionDto();
    oidc.setClientId(UUID.randomUUID().toString());
    oidc.setDiscoveryUrl("https://test.com");
    request.setOidc(oidc);

    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(orgId),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    List<Endpoint> auth0Endpoints = new ArrayList<>();

    String connections = "/api/v2/connections";
    String connectionById = String.format("%s/%s", connections, connectionId);

    String orgById = String.format("/api/v2/organizations/%s", orgId);
    String enabledConnections = String.format("%s/enabled_connections", orgById);
    String enabledConnectionById = String.format("%s/%s", enabledConnections, connectionId);
    String members = String.format("%s/members", orgById);
    String invitations = String.format("%s/invitations", orgById);
    String invitationById = String.format("%s/invitations/%s", orgById, invitationId);

    // connections
    auth0Endpoints.add(new Endpoint(HttpMethod.DELETE, connectionById));
    auth0Endpoints.add(new Endpoint(HttpMethod.POST, connections));

    // organization
    auth0Endpoints.add(new Endpoint(HttpMethod.GET, orgById));
    auth0Endpoints.add(new Endpoint(HttpMethod.PATCH, orgById));

    // enabled connections
    auth0Endpoints.add(new Endpoint(HttpMethod.DELETE, enabledConnectionById));

    // members
    auth0Endpoints.add(new Endpoint(HttpMethod.GET, members));
    auth0Endpoints.add(new Endpoint(HttpMethod.DELETE, members));

    // invitations
    auth0Endpoints.add(new Endpoint(HttpMethod.GET, invitations));
    auth0Endpoints.add(new Endpoint(HttpMethod.DELETE, invitationById));

    for (Endpoint auth0Endpoint : auth0Endpoints) {
      MockServerHelper.verify(
          1,
          auth0Endpoint.getHttpMethod(),
          auth0Endpoint.getPath(),
          AuthContextConstants.AUTH0_ACCESS_TOKEN);
    }
  }

  //  @Test
  //  void test_revokeInvitation() {
  //    Mono<HttpResponse<Void>> responseMono =
  //        mgmtOrganizationController.revokeInvitation(
  //            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
  // getAuthenticationToken());
  //    Assertions.assertThat(responseMono).isNotNull();
  //  }
  //
  //  @Test
  //  void test_block() {
  //    doReturn(mock(User.class))
  //        .when(organizationService)
  //        .changeMemberStatus(anyString(), anyString(), anyBoolean(), any());
  //    Mono<HttpResponse<User>> responseMono =
  //        mgmtOrganizationController.changeMemberStatus(
  //            UUID.randomUUID().toString(),
  //            UUID.randomUUID().toString(),
  //            false,
  //            getAuthenticationToken());
  //    Assertions.assertThat(responseMono).isNotNull();
  //  }
  //
  //  @Test
  //  void test_resetPassword() {
  //    Mono<HttpResponse<Void>> responseMono =
  //        mgmtOrganizationController.resetPassword(
  //            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
  // getAuthenticationToken());
  //    Assertions.assertThat(responseMono).isNotNull();
  //  }
  //
  //  @Test
  //  void test_updateMemberInfo() {
  //    doReturn(mock(User.class))
  //        .when(organizationService)
  //        .changeMemberStatus(anyString(), anyString(), anyBoolean(), any());
  //    MemberInfoUpdateDto memberInfoUpdateDto = new MemberInfoUpdateDto();
  //    memberInfoUpdateDto.setFamilyName("familyName");
  //    memberInfoUpdateDto.setGivenName("givenName");
  //    Mono<HttpResponse<User>> responseMono =
  //        mgmtOrganizationController.updateMemberInfo(
  //            UUID.randomUUID().toString(),
  //            UUID.randomUUID().toString(),
  //            memberInfoUpdateDto,
  //            getAuthenticationToken());
  //    Assertions.assertThat(responseMono).isNotNull();
  //  }
  //
  //  private JwtAuthenticationToken getAuthenticationToken() {
  //    Jwt jwt =
  //        Jwt.withTokenValue("token").subject("test").header("Authorization", "Bearer ").build();
  //    return new JwtAuthenticationToken(jwt);
  //  }
}
