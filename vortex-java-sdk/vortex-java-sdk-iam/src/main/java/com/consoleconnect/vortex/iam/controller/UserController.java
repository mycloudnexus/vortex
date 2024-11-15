package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "User APIs")
@Slf4j
public class UserController {

  private final UserService userService;

  @Operation(summary = "Retrieve current user's information")
  @GetMapping("/userinfo")
  public Mono<HttpResponse<com.consoleconnect.vortex.iam.dto.UserInfo>> getUserInfo(
      JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(userService.getInfo(jwt.getName())));
  }

  @Operation(summary = "Retrieve current user's authentication token")
  @GetMapping("/auth/token")
  public Mono<HttpResponse<JwtAuthenticationToken>> getAuthToken(JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(jwt));
  }

  @Operation(summary = "Retrieve current user's information from downstream.")
  @GetMapping("/downstream/userinfo")
  public Mono<HttpResponse<UserInfo>> downstreamUserInfo(JwtAuthenticationToken jwt) {
    return Mono.just(
        HttpResponse.ok(userService.downstreamUserInfo(jwt.getName(), jwt.getToken())));
  }
}
