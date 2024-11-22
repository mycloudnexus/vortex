package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.service.AuthTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/auth/token", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Auth APIs")
@Slf4j
public class AuthTokenController {

  private AuthTokenService authTokenService;

  @Operation(summary = "Retrieve current user's info")
  @GetMapping()
  public Mono<HttpResponse<AuthToken>> getAuthToken(JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(authTokenService.getAuthToken(jwt)));
  }
}
