package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.config.EmailServiceMockHelper;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.CreateInvitationDto;
import com.consoleconnect.vortex.iam.dto.MemberInfoUpdateDto;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.service.EmailService;
import com.consoleconnect.vortex.test.*;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import java.util.Map;
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

  private final WebTestClientHelper webTestClient;
  private final IamProperty iamProperty;

  @SpyBean private EmailService emailService;

  private EmailServiceMockHelper emailServiceMockHelper;

  @Autowired
  public OrganizationControllerTest(WebTestClient webTestClient, IamProperty iamProperty) {
    this.webTestClient = new WebTestClientHelper(webTestClient);
    this.iamProperty = iamProperty;
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
      emailServiceMockHelper = new EmailServiceMockHelper(emailService, iamProperty);
    }
    emailServiceMockHelper.setUp();
  }

  @Test
  @Order(1)
  void givenOrganizationInitialized_whenGetUserInfo_thenReturn200() {

    String endpoint = "/auth/token";
    String auth0Endpoint = "/api/v2/users/%s";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
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
  void givenOrganizationInitialized_whenRetrieveOrganization_thenReturn200() {

    String endpoint = "/organization";
    String auth0Endpoint = "/api/v2/organizations/%s";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(3)
  void givenOrganizationInitialized_whenRetrieveConnection_thenReturn200() {

    String endpoint = "/organization/connection";
    String auth0Endpoint = "/api/v2/organizations/%s/enabled_connections";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(4)
  void givenOrganizationInitialized_whenRetrieveMembers_thenReturn200() {

    String endpoint = "/organization/members";
    String auth0Endpoint =
        "/api/v2/organizations/%s/members?per_page=%d&page=0&include_totals=true";

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    String url =
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE);
    MockServerHelper.verify(1, url, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(5)
  void givenOrganizationInitialized_whenRetrieveRoles_thenReturn200() {

    String endpoint = "/organization/roles";
    String auth0Endpoint = "/api/v2/roles";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(1, auth0Endpoint, AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(6)
  void givenOrganizationInitialized_whenRetrieveInvitations_thenReturn200() {

    String endpoint = "/organization/invitations";
    String auth0Endpoint =
        "/api/v2/organizations/%s/invitations?per_page=%d&page=0&include_totals=true";
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    MockServerHelper.verify(
        1,
        String.format(
            auth0Endpoint, AuthContextConstants.CUSTOMER_COMPANY_ID, PagingHelper.DEFAULT_SIZE),
        AuthContextConstants.AUTH0_ACCESS_TOKEN);
  }

  @Test
  @Order(7)
  void givenOrganizationInitialized_whenCreateInvitation_thenReturn200() {

    String endpoint = "/organization/invitations";
    String auth0Endpoint = "/api/v2/organizations/%s/invitations";

    CreateInvitationDto createInvitationDto = new CreateInvitationDto();
    createInvitationDto.setEmail("fake-2@fake.com");
    createInvitationDto.setRoles(List.of(RoleEnum.ORG_ADMIN.name(), RoleEnum.ORG_MEMBER.name()));
    createInvitationDto.setSendEmail(false);
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
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
  void givenNotSupportedRole_whenCreateInvitation_thenReturn400() {

    String endpoint = "/organization/invitations";

    CreateInvitationDto createInvitationDto = new CreateInvitationDto();
    createInvitationDto.setEmail("fake-2@fake.com");
    createInvitationDto.setRoles(List.of(UUID.randomUUID().toString()));
    createInvitationDto.setSendEmail(false);
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        createInvitationDto,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(8)
  void givenSSOMember_whenResetPassword_thenReturn400() {
    String endpoint = "/organization/reset-password";
    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(endpoint).build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        null,
        400,
        Assertions::assertNotNull);
  }

  @Test
  @Order(9)
  void givenSSOMember_updateMemberInfo_thenReturn400() {
    MemberInfoUpdateDto memberInfoUpdateDto = new MemberInfoUpdateDto();
    memberInfoUpdateDto.setFamilyName("familyName");
    memberInfoUpdateDto.setGivenName("givenName");

    webTestClient.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder -> uriBuilder.path("/organization/members").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.CUSTOMER_ACCESS_TOKEN),
        memberInfoUpdateDto,
        400,
        Assertions::assertNotNull);
  }
}
