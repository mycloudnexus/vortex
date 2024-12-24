package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.model.AppProperty;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.config.EmailServiceMockHelper;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.CreateInvitationDto;
import com.consoleconnect.vortex.iam.dto.UpdateMemberDto;
import com.consoleconnect.vortex.iam.dto.UserSignupDto;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.service.EmailService;
import com.consoleconnect.vortex.test.*;
import com.consoleconnect.vortex.test.user.TestUser;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
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
class OrganizationControllerTest extends AbstractIntegrationTest {

  private final IamProperty iamProperty;
  private final AppProperty appProperty;

  @SpyBean private EmailService emailService;

  private EmailServiceMockHelper emailServiceMockHelper;

  private final TestUser mgmtUser;
  private final TestUser anonymousUser;
  private final TestUser customerUser;

  @Autowired
  public OrganizationControllerTest(
      WebTestClient webTestClient, IamProperty iamProperty, AppProperty appProperty) {
    this.iamProperty = iamProperty;
    this.appProperty = appProperty;

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
    MockServerHelper.setupMock("auth0");

    if (emailServiceMockHelper == null) {
      emailServiceMockHelper = new EmailServiceMockHelper(emailService, appProperty);
    }
    emailServiceMockHelper.setUp();
  }

  @Test
  @Order(1)
  void givenCustomerUser_whenGetUserInfo_thenReturn200() {

    String endpoint = "/auth/token";
    String auth0Endpoint = "/api/v2/users/%s";
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(
            auth0Endpoint, UriUtils.encodePath(AuthContextConstants.CUSTOMER_USER_ID, "UTF-8")),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(2)
  void givenAnonymousUser_whenRetrieveOrganization_thenReturn401() {

    String endpoint = "/organization";

    anonymousUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        401,
        Assertions::assertNull);
  }

  @Test
  @Order(2)
  void givenMgmtUser_whenRetrieveOrganization_thenReturn403() {
    String endpoint = "/organization";
    mgmtUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        403,
        Assertions::assertNull);
  }

  @Test
  @Order(2)
  void givenCustomerUser_whenRetrieveOrganization_thenReturn200() {

    String endpoint = "/organization";
    String auth0Endpoint = "/api/v2/organizations/%s";
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(4)
  void givenCustomerUser_whenRetrieveMembers_thenReturn200() {

    String endpoint = "/organization/members";
    String auth0Endpoint =
        "/api/v2/organizations/%s/members?per_page=%d&page=0&include_totals=true";

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    String url =
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(5)
  void givenCustomerUser_whenRetrieveRoles_thenReturn200() {

    String endpoint = "/organization/roles";
    String auth0Endpoint = "/api/v2/roles";
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(1, auth0Endpoint, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(6)
  void givenCustomerUser_whenRetrieveInvitations_thenReturn200() {

    String endpoint = "/organization/invitations";
    String auth0Endpoint =
        "/api/v2/organizations/%s/invitations?per_page=%d&page=0&include_totals=true";
    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(6)
  void givenCustomerUser_whenRetrieveInvitationById_thenReturn200() {

    String endpoint = "/organization/invitations/{invitationId}";

    customerUser.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(MgmtOrganizationControllerTest.INVITATION_ID),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.GET,
        String.format(
            "/api/v2/organizations/%s/invitations/%s",
            AuthContextConstants.CUSTOMER_COMPANY_ID, MgmtOrganizationControllerTest.INVITATION_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(7)
  void givenCustomerUser_whenCreateInvitation_thenReturn200() {

    String endpoint = "/organization/invitations";
    String auth0Endpoint = "/api/v2/organizations/%s/invitations";

    CreateInvitationDto createInvitationDto = new CreateInvitationDto();
    createInvitationDto.setEmail("fake-2@fake.com");
    createInvitationDto.setRoles(List.of(RoleEnum.ORG_ADMIN.name(), RoleEnum.ORG_MEMBER.name()));
    createInvitationDto.setSendEmail(false);
    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        createInvitationDto,
        200,
        Assertions::assertNotNull);

    // verify invitation created
    MockServerHelper.verify(
        1,
        HttpMethod.POST,
        String.format(auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);

    // verify email sent
    emailServiceMockHelper.verifyInvitation(
        createInvitationDto.getEmail(),
        iamProperty.getEmail().getSendGrid().getTemplates().getOrgMemberInvitation(),
        AuthContextConstants.CUSTOMER_USER_NAME);
  }

  @Test
  @Order(7)
  void givenCustomerUser_whenCreateInvitationWithNotSupportedRole_thenReturn400() {

    String endpoint = "/organization/invitations";

    CreateInvitationDto createInvitationDto = new CreateInvitationDto();
    createInvitationDto.setEmail("fake-2@fake.com");
    createInvitationDto.setRoles(List.of(UUID.randomUUID().toString()));
    createInvitationDto.setSendEmail(false);
    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        createInvitationDto,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(8)
  void givenCustomerUser_whenResetPasswordOnSsoConnection_thenReturn400() {
    String endpoint = "/organization/reset-password";
    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(8)
  void givenCustomerUser_resetPasswordOnUsernamePasswordConnection_thenReturn200() {
    String endpoint = "/organization/reset-password";

    MockServerHelper.setupMock("auth0/organization/connection/username-password");

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(1, HttpMethod.POST, "/dbconnections/change_password");
  }

  @Test
  @Order(9)
  void givenCustomerUser_updateMemberInfoOnSsoConnection_thenReturn400() {
    UpdateMemberDto updateMemberDto = new UpdateMemberDto();
    updateMemberDto.setFamilyName("familyName");
    updateMemberDto.setGivenName("givenName");

    customerUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path("/organization/members").build(),
        updateMemberDto,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(9)
  void givenCustomerUser_updateMemberInfoOnUsernamePasswordConnection_thenReturn200() {

    MockServerHelper.setupMock("auth0/organization/connection/username-password");
    UpdateMemberDto updateMemberDto = new UpdateMemberDto();
    updateMemberDto.setFamilyName("familyName");
    updateMemberDto.setGivenName("givenName");

    customerUser.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path("/organization/members").build(),
        updateMemberDto,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        HttpMethod.PATCH,
        "/api/v2/users/"
            + UriUtils.encodePath(AuthContextConstants.CUSTOMER_USER_ID, StandardCharsets.UTF_8),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(9)
  void givenCustomerUser_whenSignup_thenReturn200() {
    MockServerHelper.setupMock("auth0/signup/success");

    UserSignupDto userSignupDto = new UserSignupDto();
    userSignupDto.setPassword("11111a00000A");
    userSignupDto.setOrgId("org_0bcbzk1UJV9CvwAU");
    userSignupDto.setInvitationId("uinv_WuhQogrsLDMF8L8y");
    userSignupDto.setGivenName("fake");
    userSignupDto.setFamilyName("name");

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/organization/signup").build(),
        userSignupDto,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1, HttpMethod.POST, "/api/v2/users", AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(9)
  void givenCustomerUser_whenSignup_thenReturnExpired() {
    UserSignupDto userSignupDto = new UserSignupDto();
    userSignupDto.setPassword("11111a00000A");
    userSignupDto.setOrgId("org_0bcbzk1UJV9CvwAU");
    userSignupDto.setInvitationId("uinv_WuhQogrsLDMF8L8y");
    userSignupDto.setGivenName("fake");
    userSignupDto.setFamilyName("name");

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/organization/signup").build(),
        userSignupDto,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(9)
  void givenCustomerUser_whenSignup_inactiveOrganization_thenReturn400() {
    MockServerHelper.setupMock("auth0/signup/success");
    MockServerHelper.setupMock("auth0/signup/inactive_organization");

    UserSignupDto userSignupDto = new UserSignupDto();
    userSignupDto.setPassword("11111a00000A");
    userSignupDto.setOrgId("org_0bcbzk1UJV9CvwAU");
    userSignupDto.setInvitationId("uinv_WuhQogrsLDMF8L8y");
    userSignupDto.setGivenName("fake");
    userSignupDto.setFamilyName("name");

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/organization/signup").build(),
        userSignupDto,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(9)
  void givenCustomerUser_whenSignup_auth0_exception_thenReturn400() {
    UserSignupDto userSignupDto = new UserSignupDto();
    userSignupDto.setPassword("11111a00000A");
    userSignupDto.setOrgId("org_0bcbzk1UJV9CvwAU");
    userSignupDto.setInvitationId("uinv_WuhQogrsLDMF8L8y");
    userSignupDto.setGivenName("fake");
    userSignupDto.setFamilyName("name");

    MockServerHelper.requestException(
        HttpMethod.DELETE,
        String.format(
            "api/v2/organizations/%s", UriUtils.encodePath(userSignupDto.getOrgId(), "UTF-8")));

    customerUser.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/organization/signup").build(),
        userSignupDto,
        400,
        Assertions::assertNotNull);
  }
}
