package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.service.AuthTokenService;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customer Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtOrganizationController {

  private final OrganizationService service;
  private final AuthTokenService authTokenService;

  @Operation(summary = "List all existing organizations")
  @GetMapping("")
  public Mono<HttpResponse<Paging<OrganizationInfo>>> search(
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info("search, page:{}, size:{}", page, size);
    return Mono.just(HttpResponse.ok(service.search(page, size)));
  }

  @Operation(summary = "Create a new organization")
  @PostMapping("")
  public Mono<HttpResponse<OrganizationInfo>> create(
      @Validated @RequestBody CreateOrganizationDto request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.just(HttpResponse.ok(service.create(request, authenticationToken.getName())));
  }

  @Operation(summary = "update a organization")
  @PatchMapping("/{orgId}")
  public Mono<HttpResponse<OrganizationInfo>> update(
      @PathVariable String orgId,
      @Validated @RequestBody UpdateOrganizationDto request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.update(orgId, request, authenticationToken.getName())));
  }

  @Operation(summary = "Retrieve an organization by id")
  @GetMapping("/{orgId}")
  public Mono<HttpResponse<OrganizationInfo>> findOne(@PathVariable String orgId) {
    return Mono.just(HttpResponse.ok(service.findOne(orgId)));
  }

  @Operation(summary = "Setup a connection")
  @PostMapping("/{orgId}/connection")
  public Mono<HttpResponse<Connection>> createConnection(
      @PathVariable String orgId,
      @RequestBody CreateConnectionDto request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.createConnection(orgId, request, authenticationToken.getName())));
  }

  @Operation(summary = "List all invitations")
  @GetMapping("/{orgId}/invitations")
  public Mono<HttpResponse<Paging<Invitation>>> listInvitations(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return Mono.just(HttpResponse.ok(service.listInvitations(orgId, page, size)));
  }

  @Operation(summary = "Create a new invitation")
  @PostMapping("/{orgId}/invitations")
  public Mono<HttpResponse<Invitation>> createInvitation(
      @PathVariable String orgId,
      @Validated @RequestBody CreateInvitationDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {

    return Mono.just(
        HttpResponse.ok(
            service.createInvitation(
                orgId, request, authTokenService.getAuthToken(jwtAuthenticationToken))));
  }

  @Operation(summary = "Retrieve an invitation by id")
  @GetMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Invitation>> findInvitationById(
      @PathVariable String orgId, @PathVariable String invitationId) {
    return Mono.just(HttpResponse.ok(service.getInvitationById(orgId, invitationId)));
  }

  @Operation(summary = "List all members")
  @GetMapping("/{orgId}/members")
  public Mono<HttpResponse<Paging<MemberInfo>>> listMembers(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return Mono.just(HttpResponse.ok(service.listMembers(orgId, page, size)));
  }

  @Operation(summary = "Retrieve a member details")
  @GetMapping("/{orgId}/members/{memberId}")
  public Mono<HttpResponse<User>> findMemberById(
      @PathVariable String orgId, @PathVariable String memberId) {
    return Mono.just(HttpResponse.ok(service.findUserById(orgId, memberId)));
  }

  @Operation(summary = "Update a member")
  @PatchMapping("/{orgId}/members/{memberId}")
  public Mono<HttpResponse<User>> updateMember(
      @PathVariable String orgId,
      @PathVariable String memberId,
      @RequestBody UpdateMemberDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.updateMember(orgId, memberId, request, jwtAuthenticationToken.getName())));
  }

  @Operation(summary = "List all roles")
  @GetMapping("/{orgId}/roles")
  public Mono<HttpResponse<Paging<Role>>> listRoles(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return Mono.just(HttpResponse.ok(service.listRoles(orgId, page, size)));
  }

  @Operation(summary = "Update a connection")
  @PatchMapping("/{orgId}/connection")
  public Mono<HttpResponse<Connection>> updateConnection(
      @PathVariable String orgId,
      @Valid @RequestBody UpdateConnectionDto updateConnectionDto,
      JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.updateConnection(orgId, updateConnectionDto, authenticationToken.getName())));
  }

  @Operation(summary = "Revoke an invitation by id")
  @DeleteMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Void>> revokeInvitation(
      @PathVariable String orgId,
      @PathVariable String invitationId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.revokeInvitation(orgId, invitationId, jwtAuthenticationToken.getName())));
  }

  @Operation(summary = "Trigger resetting a user's password")
  @PostMapping("/{orgId}/members/{memberId}/reset-password")
  public Mono<HttpResponse<Void>> resetPassword(
      @PathVariable String orgId,
      @PathVariable String memberId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.resetPassword(orgId, memberId, jwtAuthenticationToken.getName())));
  }

  @Operation(summary = "Delete a member.")
  @DeleteMapping("/{orgId}/members/{memberId}")
  public Mono<HttpResponse<User>> deleteMember(
      @PathVariable String orgId,
      @PathVariable String memberId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    service.deleteMember(orgId, memberId, jwtAuthenticationToken.getName());
    return Mono.just(HttpResponse.ok(null));
  }
}
