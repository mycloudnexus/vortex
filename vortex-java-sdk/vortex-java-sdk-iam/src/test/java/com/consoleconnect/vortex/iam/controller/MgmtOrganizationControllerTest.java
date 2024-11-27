package com.consoleconnect.vortex.iam.controller;

import static org.mockito.ArgumentMatchers.any;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.cc.model.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Endpoint;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.service.UserService;
import com.consoleconnect.vortex.iam.toolkit.Auth0PageHelper;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriUtils;

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

  public static final String ORG_ID = "org_0bcbzk1UJV9CvwAU";
  public static final String CONNECTION_ID = "con_YNEZH8rgZ8sQz9Fq";
  public static final String INVITATION_ID = "uinv_WuhQogrsLDMF8L8y";
  public static final String USER_ID = "vortex-test|auth0|5ec4d3765cf0a1001486b95d";

  @SpyBean private UserService userService;

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
  void givenMgmtUser_whenSearchOrganizations_thenReturn200() {
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
  void givenMgmtUser_whenSearchMembers_thenReturn200() {
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
  void givenMgmtUser_whenRetrieveMemberById_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/members/{memberId}";

    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder.path(endpoint).build(AuthContextConstants.CUSTOMER_COMPANY_ID, USER_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/v2/users/%s", UriUtils.encodePath(USER_ID, "UTF-8")),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_whenBlockMemberById_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/members/{memberId}";

    UpdateMemberDto request = new UpdateMemberDto();
    request.setBlocked(true);
    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder ->
            uriBuilder.path(endpoint).build(AuthContextConstants.CUSTOMER_COMPANY_ID, USER_ID),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.PATCH,
        String.format("/api/v2/users/%s", UriUtils.encodePath(USER_ID, "UTF-8")),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_whenUpdateMemberById_thenReturn400() {
    String endpoint = "/mgmt/organizations/{orgId}/members/{memberId}";

    UpdateMemberDto request = new UpdateMemberDto();
    request.setFamilyName("test");
    request.setGivenName("test");
    // given default page and size
    mgmtUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder ->
            uriBuilder.path(endpoint).build(AuthContextConstants.CUSTOMER_COMPANY_ID, USER_ID),
        request,
        400,
        org.junit.jupiter.api.Assertions::assertNotNull);
  }

  @Test
  void givenMgmtUser_whenSearchInvitations_thenReturn200() {
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
  void givenMgmtUser_whenRetrieveOrganization_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}";

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/v2/organizations/%s", ORG_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_whenUpdateOrganization_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}";

    UpdateOrganizationDto request = new UpdateOrganizationDto();
    request.setDisplayName("test-updated");

    mgmtUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.PATCH,
        String.format("/api/v2/organizations/%s", ORG_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_whenDisableOrganization_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}";

    UpdateOrganizationDto request = new UpdateOrganizationDto();
    request.setStatus(OrgStatusEnum.INACTIVE);

    mgmtUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.PATCH,
        String.format("/api/v2/organizations/%s", ORG_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_whenEnableOrganizationInActiveStatus_thenReturn400() {
    String endpoint = "/mgmt/organizations/{orgId}";

    UpdateOrganizationDto request = new UpdateOrganizationDto();
    request.setStatus(OrgStatusEnum.ACTIVE);

    mgmtUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        request,
        400,
        org.junit.jupiter.api.Assertions::assertNotNull);
  }

  @Test
  void givenMgmtUser_thenCreateOidcConnection_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/connection";

    CreateConnectionDto request = new CreateConnectionDto();
    request.setStrategy(ConnectionStrategyEnum.OIDC);
    OidcConnectionDto oidc = new OidcConnectionDto();
    oidc.setClientId(UUID.randomUUID().toString());
    oidc.setDiscoveryUrl("https://test.com");
    request.setOidc(oidc);

    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    List<Endpoint> auth0Endpoints = new ArrayList<>();

    String connections = "/api/v2/connections";
    String connectionById = String.format("%s/%s", connections, CONNECTION_ID);

    String orgById = String.format("/api/v2/organizations/%s", ORG_ID);
    String enabledConnections = String.format("%s/enabled_connections", orgById);
    String enabledConnectionById = String.format("%s/%s", enabledConnections, CONNECTION_ID);
    String members = String.format("%s/members", orgById);
    String invitations = String.format("%s/invitations", orgById);
    String invitationById = String.format("%s/invitations/%s", orgById, INVITATION_ID);

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

  @Test
  void givenMgmtUser_thenCreateInvitation_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/invitations";

    User user = new User();
    user.setName("test");
    user.setEmail("hello@hello.com");
    Mockito.doReturn(user).when(userService).getUserInfo(any(JwtAuthenticationToken.class));

    CreateInvitationDto request = new CreateInvitationDto();
    request.setSendEmail(true);
    request.setEmail("test@test.com");
    request.setRoles(List.of(RoleEnum.ORG_ADMIN.name()));

    mgmtUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        request,
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.POST,
        String.format("/api/v2/organizations/%s/invitations", ORG_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_thenRetrieveInvitationById_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/invitations/{invitationId}";

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID, INVITATION_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format("/api/v2/organizations/%s/invitations/%s", ORG_ID, INVITATION_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_thenRevokeInvitationById_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/invitations/{invitationId}";

    mgmtUser.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID, INVITATION_ID),
        200,
        org.junit.jupiter.api.Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.DELETE,
        String.format("/api/v2/organizations/%s/invitations/%s", ORG_ID, INVITATION_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  void givenMgmtUser_thenListRoles_thenReturn200() {
    String endpoint = "/mgmt/organizations/{orgId}/roles";

    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(ORG_ID),
        200,
        res -> {
          log.info("{}", res);
          HttpResponse<Paging<Role>> roles = JsonToolkit.fromJson(res, new TypeReference<>() {});
          Assertions.assertEquals(2, roles.getData().getData().size());
        });

    MockServerHelper.verify(
        1, HttpMethod.GET, "/api/v2/roles", AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }
}
