package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.dto.UserInfo;
import com.consoleconnect.vortex.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "User APIs")
@Slf4j
public class UserInfoController {

  private final UserService userService;

  @Operation(summary = "Retrieve current user's information")
  @GetMapping("")
  public HttpResponse<UserInfo> getAuthToken(JwtAuthenticationToken jwt) {
    return HttpResponse.ok(userService.getInfo(jwt.getName()));
  }
}
