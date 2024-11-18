package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Member;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.CreateInvitationDto;
import com.consoleconnect.vortex.iam.dto.OrganizationConnection;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.UserContextService;
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
@RequestMapping(value = "/organization", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organization", description = "Organization APIs")
@Slf4j
public class OrganizationController {

  private final OrganizationService service;
  private final UserContextService userContextService;

  @Operation(summary = "Retrieve an organization by id")
  @GetMapping()
  public Mono<HttpResponse<Organization>> findOne() {
    return userContextService.getOrgId().map(orgId -> HttpResponse.ok(service.findOne(orgId)));
  }

  @Operation(summary = "List all existing connections")
  @GetMapping("/connections")
  public Mono<HttpResponse<Paging<OrganizationConnection>>> listConnections(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.listConnections(orgId, page, size)));
  }

  @Operation(summary = "Setup a connection")
  @PostMapping("/connections")
  public Mono<HttpResponse<OrganizationConnection>> createConnection(
      @RequestBody CreateConnectionDto request, JwtAuthenticationToken authenticationToken) {
    return userContextService
        .getOrgId()
        .map(
            orgId ->
                HttpResponse.ok(
                    service.createConnection(orgId, request, authenticationToken.getName())));
  }

  @Operation(summary = "List all invitations")
  @GetMapping("/invitations")
  public Mono<HttpResponse<Paging<Invitation>>> listInvitations(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.listInvitations(orgId, page, size)));
  }

  @Operation(summary = "Create a new invitation")
  @PostMapping("/invitations")
  public Mono<HttpResponse<Invitation>> create(
      @RequestBody CreateInvitationDto request, JwtAuthenticationToken jwtAuthenticationToken) {
    return userContextService
        .getOrgId()
        .map(
            orgId ->
                HttpResponse.ok(
                    service.createInvitation(orgId, request, jwtAuthenticationToken.getName())));
  }

  @Operation(summary = "Retrieve an invitation by id")
  @GetMapping("/invitations/{invitationId}")
  public Mono<HttpResponse<Invitation>> findOne(@PathVariable String invitationId) {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.getInvitationById(orgId, invitationId)));
  }

  @Operation(summary = "Delete an invitation by id")
  @DeleteMapping("/invitations/{invitationId}")
  public Mono<HttpResponse<Void>> delete(
      @PathVariable String invitationId, JwtAuthenticationToken jwtAuthenticationToken) {

    return userContextService
        .getOrgId()
        .map(
            orgId -> {
              service.deleteInvitation(orgId, invitationId, jwtAuthenticationToken.getName());
              return HttpResponse.ok(null);
            });
  }

  @Operation(summary = "List all members")
  @GetMapping("/members")
  public Mono<HttpResponse<Paging<Member>>> listMembers(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.listMembers(orgId, page, size)));
  }

  @Operation(summary = "List all roles")
  @GetMapping("/roles")
  public Mono<HttpResponse<Paging<Role>>> listRoles(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.listRoles(orgId, page, size)));
  }
}
