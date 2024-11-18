package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.dto.MemberInfo;
import com.consoleconnect.vortex.iam.service.MemberService;
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
@Tag(name = "Member", description = "Member APIs")
@Slf4j
public class MemberController {

  private final MemberService service;

  @Operation(summary = "Retrieve current member's information")
  @GetMapping("/userinfo")
  public Mono<HttpResponse<MemberInfo>> getUserInfo(JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(service.getInfo(jwt.getName())));
  }
}
