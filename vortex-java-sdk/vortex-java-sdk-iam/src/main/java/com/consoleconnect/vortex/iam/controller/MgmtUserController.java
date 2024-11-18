package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.service.UserService;
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
@RequestMapping(value = "/mgmt/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtUserController {

  private final UserService service;

  @Operation(summary = "List all existing users")
  @GetMapping("")
  public Mono<HttpResponse<Paging<User>>> search(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(HttpResponse.ok(service.search(page, size, jwtAuthenticationToken)));
  }

  @Operation(summary = "Retrieve an user by id")
  @GetMapping("/{userId}")
  public Mono<HttpResponse<User>> findOne(
      @PathVariable String userId, JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(HttpResponse.ok(service.getUserInfo(userId, jwtAuthenticationToken)));
  }

  @Operation(summary = "Update an user by id")
  @PatchMapping("/{userId}")
  public Mono<HttpResponse<User>> update(
      @PathVariable String userId,
      @RequestBody UpdateUserDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(HttpResponse.ok(service.update(userId, request, jwtAuthenticationToken)));
  }

  @Operation(summary = "Delete an user by id")
  @DeleteMapping("/{userId}")
  public Mono<HttpResponse<Void>> delete(
      @PathVariable String userId, JwtAuthenticationToken jwtAuthenticationToken) {
    service.delete(userId, jwtAuthenticationToken);
    return Mono.just(HttpResponse.ok(null));
  }

  @Operation(summary = "Add a new user")
  @PostMapping("")
  public Mono<HttpResponse<User>> create(
      @RequestBody CreateUserDto request, JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(HttpResponse.ok(service.create(request, jwtAuthenticationToken)));
  }

  @Operation(summary = "Retrieve current user's information")
  @GetMapping("/userinfo")
  public Mono<HttpResponse<User>> getUserInfo(JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(service.getUserInfo(null, jwt)));
  }

  @Operation(summary = "Retrieve current user's authentication token")
  @GetMapping("/auth/token")
  public Mono<HttpResponse<JwtAuthenticationToken>> getAuthToken(JwtAuthenticationToken jwt) {
    return Mono.just(HttpResponse.ok(jwt));
  }
}
