package com.consoleconnect.vortex.iam.controller;

import static org.mockito.Mockito.mock;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.UserService;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

class UserControllerTest {
  private UserService userService = mock(UserService.class);
  private OrganizationService organizationService = mock(OrganizationService.class);
  private UserController userController = new UserController(userService, organizationService);

  @Test
  void test_reset() {
    Mono<HttpResponse<Void>> responseMono =
        userController.reset(UUID.randomUUID().toString(), getAuthenticationToken());
    Assertions.assertThat(responseMono).isNotNull();
  }

  private JwtAuthenticationToken getAuthenticationToken() {
    Jwt jwt =
        Jwt.withTokenValue("token").subject("test").header("Authorization", "Bearer ").build();
    return new JwtAuthenticationToken(jwt);
  }
}
