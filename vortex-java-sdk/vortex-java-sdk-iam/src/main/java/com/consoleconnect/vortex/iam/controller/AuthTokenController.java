package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/auth/token", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "User APIs")
@Slf4j
public class AuthTokenController {

  @Operation(summary = "Retrieve current user's authentication token")
  @GetMapping("")
  public HttpResponse<JwtAuthenticationToken> getAuthToken(JwtAuthenticationToken jwt) {
    return HttpResponse.ok(jwt);
  }
}
