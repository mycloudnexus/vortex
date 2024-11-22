package com.consoleconnect.vortex.iam.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.core.exception.VortexError;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.CreateUserDto;
import com.consoleconnect.vortex.iam.dto.UpdateUserDto;
import com.consoleconnect.vortex.iam.dto.User;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import com.consoleconnect.vortex.test.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
class MgmtUserControllerTest extends AbstractIntegrationTest {

  private final WebTestClientHelper webTestClient;

  public MgmtUserControllerTest(@Autowired WebTestClient webTestClient) {
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
  @Order(1)
  void givenInitialized_whenListUsers_thenReturn200() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/users").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        200,
        response -> {
          HttpResponse<Paging<User>> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertFalse(res.getData().getData().isEmpty());

          for (User user : res.getData().getData()) {
            Assertions.assertNotNull(user.getId());
            Assertions.assertNotNull(user.getName());
            Assertions.assertNotNull(user.getEmail());
            Assertions.assertEquals(UserStatusEnum.ACTIVE, user.getStatus());
            Assertions.assertNotNull(user.getOrganizationId());
          }
        });

    MockServerHelper.verify(
        1,
        String.format("/v2/companies/%s/members?pageSize=0", AuthContextConstants.MGMT_COMPANY_ID),
        AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  @Test
  @Order(2)
  void givenCorrectUserId_whenFindOne_thenReturn200() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder ->
            uriBuilder.path("/mgmt/users/{userId}").build(AuthContextConstants.MGMT_USER_ID),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        200,
        response -> {
          HttpResponse<User> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertNotNull(res.getData());
          Assertions.assertEquals(AuthContextConstants.MGMT_USER_ID, res.getData().getId());
          Assertions.assertNotNull(res.getData().getName());
          Assertions.assertNotNull(res.getData().getEmail());
          Assertions.assertNotNull(res.getData().getOrganizationId());
          Assertions.assertEquals(UserStatusEnum.ACTIVE, res.getData().getStatus());
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
  @Order(3)
  void givenWrongUserId_whenFindOne_thenReturn404() {

    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/mgmt/users/{userId}").build(UUID.randomUUID().toString()),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        404,
        response -> {
          VortexError error = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertEquals(404, error.getCode());
          Assertions.assertNotNull(error.getReason());
          Assertions.assertNotNull(error.getReferenceError());
          Assertions.assertNotNull(error.getMessage());
        });
  }

  @Test
  @Order(4)
  void givenCorrectPayload_whenCreate_thenReturn200() {

    // before inviting user, it can't access the system
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN_2),
        null,
        401,
        Assertions::assertNull);

    // invite user
    CreateUserDto createUserDto = new CreateUserDto();
    createUserDto.setUserId(AuthContextConstants.MGMT_USER_ID_2);
    createUserDto.setRoles(List.of(RoleEnum.ORG_ADMIN.toString()));
    createUserDto.setSendEmail(false);

    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/mgmt/users").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        createUserDto,
        200,
        response -> {
          HttpResponse<User> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertNotNull(res.getData());
          Assertions.assertEquals(AuthContextConstants.MGMT_USER_ID_2, res.getData().getId());
          Assertions.assertNotNull(res.getData().getName());
          Assertions.assertNotNull(res.getData().getEmail());
          Assertions.assertNotNull(res.getData().getOrganizationId());
          Assertions.assertEquals(UserStatusEnum.ACTIVE, res.getData().getStatus());
        });

    // invited user should be able to access the system
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN_2),
        null,
        200,
        Assertions::assertNotNull);
  }

  @Test
  @Order(5)
  void givenActiveUser_whenDisable_thenReturn200() {

    // disable user
    UpdateUserDto updateUserDto = new UpdateUserDto();
    updateUserDto.setStatus(UserStatusEnum.INACTIVE);

    webTestClient.requestAndVerify(
        HttpMethod.PATCH,
        uriBuilder ->
            uriBuilder.path("/mgmt/users/{userId}").build(AuthContextConstants.MGMT_USER_ID_2),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        updateUserDto,
        200,
        response -> {
          HttpResponse<User> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertNotNull(res.getData());
          Assertions.assertEquals(AuthContextConstants.MGMT_USER_ID_2, res.getData().getId());
          Assertions.assertEquals(UserStatusEnum.INACTIVE, res.getData().getStatus());
        });

    // disabled user can't access the system
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN_2),
        null,
        401,
        Assertions::assertNull);
  }

  @Test
  @Order(6)
  void givenExistingUser_whenDelete_thenReturn200() {

    // delete a user
    webTestClient.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder ->
            uriBuilder.path("/mgmt/users/{userId}").build(AuthContextConstants.MGMT_USER_ID_2),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        null,
        200,
        Assertions::assertNotNull);

    // deleted user can't access the system
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN_2),
        null,
        401,
        Assertions::assertNull);
  }

  @Test
  @Order(7)
  void givenDeletedUser_whenCreate_thenReturn200() {
    // invite user
    CreateUserDto createUserDto = new CreateUserDto();
    createUserDto.setUserId(AuthContextConstants.MGMT_USER_ID_2);
    createUserDto.setRoles(List.of(RoleEnum.ORG_ADMIN.toString()));
    createUserDto.setSendEmail(false);

    webTestClient.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path("/mgmt/users").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN),
        createUserDto,
        200,
        response -> {
          HttpResponse<User> res = JsonToolkit.fromJson(response, new TypeReference<>() {});
          Assertions.assertNotNull(res.getData());
          Assertions.assertEquals(AuthContextConstants.MGMT_USER_ID_2, res.getData().getId());
          Assertions.assertNotNull(res.getData().getName());
          Assertions.assertNotNull(res.getData().getEmail());
          Assertions.assertNotNull(res.getData().getOrganizationId());
          Assertions.assertEquals(UserStatusEnum.ACTIVE, res.getData().getStatus());
        });

    // invited user should be able to access the system
    webTestClient.requestAndVerify(
        HttpMethod.GET,
        uriBuilder -> uriBuilder.path("/auth/token").build(),
        Map.of("Authorization", "Bearer " + AuthContextConstants.MGMT_ACCESS_TOKEN_2),
        null,
        200,
        Assertions::assertNotNull);
  }
}
