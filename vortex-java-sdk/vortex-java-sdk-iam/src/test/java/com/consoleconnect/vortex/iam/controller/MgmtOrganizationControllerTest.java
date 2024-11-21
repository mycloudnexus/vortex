package com.consoleconnect.vortex.iam.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

class MgmtOrganizationControllerTest {
  private OrganizationService organizationService = mock(OrganizationService.class);
  private MgmtOrganizationController mgmtOrganizationController =
      new MgmtOrganizationController(organizationService);

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
        .changeStatus(anyString(), anyString(), anyBoolean(), any());
    Mono<HttpResponse<User>> responseMono =
        mgmtOrganizationController.changeStatus(
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

  private JwtAuthenticationToken getAuthenticationToken() {
    Jwt jwt =
        Jwt.withTokenValue("token").subject("test").header("Authorization", "Bearer ").build();
    return new JwtAuthenticationToken(jwt);
  }
}
