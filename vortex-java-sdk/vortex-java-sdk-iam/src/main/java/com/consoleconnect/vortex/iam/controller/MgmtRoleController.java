package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.roles.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.RoleInfo;
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
@RequestMapping(value = "/mgmt/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtRoleController {

  private final UserService service;

  @PreAuthorize("hasPermission('mgmt:role', 'list')")
  @Operation(summary = "List all existing roles")
  @GetMapping("")
  public Mono<HttpResponse<Paging<Role>>> search(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return Mono.just(HttpResponse.ok(service.listRoles(page, size)));
  }

  @PreAuthorize("hasPermission('mgmt:role', 'read')")
  @Operation(summary = "Retrieve a role by id")
  @GetMapping("/{roleId}")
  public Mono<HttpResponse<RoleInfo>> findOne(@PathVariable String roleId) {
    return Mono.just(HttpResponse.ok(service.getRoleById(roleId)));
  }
}
