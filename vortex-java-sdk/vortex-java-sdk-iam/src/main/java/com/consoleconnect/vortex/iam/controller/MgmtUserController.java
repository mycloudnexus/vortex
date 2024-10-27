package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.users.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtUserController {

  private final UserService service;

  @PreAuthorize("hasPermission('mgmt:user', 'list')")
  @Operation(summary = "List all existing users")
  @GetMapping("")
  public Mono<HttpResponse<Paging<User>>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return Mono.just(HttpResponse.ok(service.searchUsers(q, email, page, size)));
  }

  @PreAuthorize("hasPermission('mgmt:user', 'read') ")
  @Operation(summary = "Retrieve an user by id")
  @GetMapping("/{userId}")
  public Mono<HttpResponse<UserInfo>> findOne(@PathVariable String userId) {
    return Mono.just(HttpResponse.ok(service.getInfo(userId)));
  }
}
