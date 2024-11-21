package com.consoleconnect.vortex.iam.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.UserContextService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

class UserOrganizationControllerTest {
  private OrganizationService organizationService = mock(OrganizationService.class);
  private UserContextService userContextService = mock(UserContextService.class);
  private UserOrganizationController userOrganizationController =
      new UserOrganizationController(organizationService, userContextService);

  @Test
  void resetPassword() {
    doReturn(Mono.empty()).when(userContextService).getOrgId();
    Mono<HttpResponse<Void>> responseMono =
        userOrganizationController.resetPassword(getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  private JwtAuthenticationToken getAuthenticationToken() {
    Jwt jwt =
        Jwt.withTokenValue("token").subject("test").header("Authorization", "Bearer ").build();
    return new JwtAuthenticationToken(jwt);
  }
}
