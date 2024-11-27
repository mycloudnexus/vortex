package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateInvitationDto;
import com.consoleconnect.vortex.iam.dto.MemberInfo;
import com.consoleconnect.vortex.iam.dto.MemberInfoUpdateDto;
import com.consoleconnect.vortex.iam.dto.OrganizationConnection;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/organization", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customer APIs", description = "Customer APIs")
@Slf4j
public class OrganizationController {

  private final OrganizationService service;
  private final UserContextService userContextService;

  @Operation(summary = "Retrieve an organization by id")
  @GetMapping()
  public Mono<HttpResponse<Organization>> findOne() {
    return userContextService.getOrgId().map(orgId -> HttpResponse.ok(service.findOne(orgId)));
  }

  @Operation(summary = "Get one bound connection")
  @GetMapping("/connection")
  public Mono<HttpResponse<OrganizationConnection>> getOneConnection() {
    return userContextService
        .getOrgId()
        .map(orgId -> HttpResponse.ok(service.getOneConnection(orgId)));
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

  @Operation(summary = "List all members")
  @GetMapping("/members")
  public Mono<HttpResponse<Paging<MemberInfo>>> listMembers(
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

  @Operation(summary = "Trigger resetting a user's password")
  @PostMapping("/reset-password")
  public Mono<HttpResponse<Void>> resetPassword(JwtAuthenticationToken jwtAuthenticationToken) {
    return userContextService
        .getOrgId()
        .map(
            orgId -> {
              service.resetPassword(
                  orgId, jwtAuthenticationToken.getName(), jwtAuthenticationToken.getName());
              return HttpResponse.ok(null);
            });
  }

  @Operation(summary = "Update the member info.")
  @PatchMapping("/members")
  public Mono<HttpResponse<User>> updateMemberInfo(
      @Validated @RequestBody MemberInfoUpdateDto memberInfoUpdateDto,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return userContextService
        .getOrgId()
        .map(
            orgId ->
                HttpResponse.ok(
                    service.updateMemberInfo(
                        orgId,
                        jwtAuthenticationToken.getName(),
                        memberInfoUpdateDto,
                        jwtAuthenticationToken.getName())));
  }
}
