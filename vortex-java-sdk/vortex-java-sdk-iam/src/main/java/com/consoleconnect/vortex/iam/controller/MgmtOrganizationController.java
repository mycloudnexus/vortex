package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Member;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping(value = "/mgmt/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mgmt", description = "Mgmt APIs")
public class MgmtOrganizationController extends BaseOrganizationController {

  public MgmtOrganizationController(OrganizationService service) {
    super(service);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'list')")
  @Operation(summary = "List all existing organizations")
  @GetMapping("")
  public Mono<HttpResponse<Paging<Organization>>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return super.search(q, page, size);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'create')")
  @Operation(summary = "Create a new organization")
  @PostMapping("")
  public Mono<HttpResponse<Organization>> create(
      @RequestBody CreateOrganizationDto request, JwtAuthenticationToken authenticationToken) {
    return super.create(request, authenticationToken);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "update a organization")
  @PatchMapping("/{orgId}")
  public Mono<HttpResponse<Organization>> update(
      @PathVariable String orgId,
      @RequestBody UpdateOrganizationDto request,
      JwtAuthenticationToken authenticationToken) {
    return super.update(orgId, request, authenticationToken);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read') ")
  @Operation(summary = "Retrieve an organization by id")
  @GetMapping("/{orgId}")
  public Mono<HttpResponse<Organization>> findOne(@PathVariable String orgId) {
    return super.findOne(orgId);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "List all existing connections")
  @GetMapping("/{orgId}/connections")
  public Mono<HttpResponse<Paging<OrganizationConnection>>> listConnections(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return super.listConnections(orgId, page, size);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Setup a connection")
  @PostMapping("/{orgId}/connections")
  public Mono<HttpResponse<OrganizationConnection>> createConnection(
      @PathVariable String orgId,
      @RequestBody CreateConnectionDto request,
      JwtAuthenticationToken authenticationToken) {
    return super.createConnection(orgId, request, authenticationToken);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "List all invitations")
  @GetMapping("/{orgId}/invitations")
  public Mono<HttpResponse<Paging<Invitation>>> listInivitations(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return super.listInivitations(orgId, page, size);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Create a new invitation")
  @PostMapping("/{orgId}/invitations")
  public Mono<HttpResponse<Invitation>> create(
      @PathVariable String orgId,
      @RequestBody CreateInivitationDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return super.create(orgId, request, jwtAuthenticationToken);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "Retrieve an invitation by id")
  @GetMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Invitation>> findOne(
      @PathVariable String orgId, @PathVariable String invitationId) {
    return super.findOne(orgId, invitationId);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Delete an invitation by id")
  @DeleteMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Void>> delete(
      @PathVariable String orgId,
      @PathVariable String invitationId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return super.delete(orgId, invitationId, jwtAuthenticationToken);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "List all members")
  @GetMapping("/{orgId}/members")
  public Mono<HttpResponse<Paging<Member>>> listMembers(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return super.listMembers(orgId, page, size);
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "List all roles")
  @GetMapping("/{orgId}/roles")
  public Mono<HttpResponse<Paging<Role>>> listRoles(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return super.listRoles(orgId, page, size);
  }
}
