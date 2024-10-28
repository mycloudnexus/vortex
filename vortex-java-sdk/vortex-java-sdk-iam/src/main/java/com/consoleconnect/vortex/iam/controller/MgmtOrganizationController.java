package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Member;
import com.auth0.json.mgmt.roles.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.CreateInivitationDto;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.dto.OrganizationConnection;
import com.consoleconnect.vortex.iam.dto.OrganizationDto;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.VortexOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtOrganizationController {

  private final VortexOrganizationService vortexOrganizationService;
  private final OrganizationService service;

  @PreAuthorize("hasPermission('mgmt:org', 'list')")
  @Operation(summary = "List all existing organizations")
  @GetMapping("")
  public Mono<HttpResponse<Paging<OrganizationDto>>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "type", required = false) OrgTypeEnum type,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info("search, q:{}, page:{}, size:{}", q, page, size);
    return Mono.just(HttpResponse.ok(vortexOrganizationService.search(q, type, page, size)));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'create')")
  @Operation(summary = "Create a new organization")
  @PostMapping("")
  public Mono<HttpResponse<OrganizationDto>> create(
      @RequestBody CreateOrganizationDto request, JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(vortexOrganizationService.create(request, authenticationToken.getName())));
  }

  @Operation(description = "update organization", summary = "update organization")
  @PatchMapping("/{orgId}")
  public HttpResponse<OrganizationDto> update(
      @PathVariable(value = "orgId") String orgId, @RequestBody CreateOrganizationDto request) {
    return HttpResponse.ok(vortexOrganizationService.update(orgId, request));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read') ")
  @Operation(summary = "Retrieve an organization by id")
  @GetMapping("/{orgId}")
  public Mono<HttpResponse<OrganizationDto>> findOne(@PathVariable String orgId) {
    return Mono.just(HttpResponse.ok(vortexOrganizationService.findOne(orgId)));
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
    return Mono.just(HttpResponse.ok(service.listConnections(orgId, page, size)));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Setup a connection")
  @PostMapping("/{orgId}/connections")
  public Mono<HttpResponse<OrganizationConnection>> createConnection(
      @PathVariable String orgId,
      @RequestBody CreateConnectionDto request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.createConnection(orgId, request, authenticationToken.getName())));
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
    return Mono.just(HttpResponse.ok(service.listInvitations(orgId, page, size)));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Create a new invitation")
  @PostMapping("/{orgId}/invitations")
  public Mono<HttpResponse<Invitation>> create(
      @PathVariable String orgId,
      @RequestBody CreateInivitationDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.createInvitation(orgId, request, jwtAuthenticationToken.getName())));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'read')")
  @Operation(summary = "Retrieve an invitation by id")
  @GetMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Invitation>> findOne(
      @PathVariable String orgId, @PathVariable String invitationId) {
    return Mono.just(HttpResponse.ok(service.getInvitationById(orgId, invitationId)));
  }

  @PreAuthorize("hasPermission('mgmt:org', 'update')")
  @Operation(summary = "Delete an invitation by id")
  @DeleteMapping("/{orgId}/invitations/{invitationId}")
  public Mono<HttpResponse<Void>> delete(
      @PathVariable String orgId,
      @PathVariable String invitationId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    service.deleteInvitation(orgId, invitationId, jwtAuthenticationToken.getName());
    return Mono.just(HttpResponse.ok(null));
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
    return Mono.just(HttpResponse.ok(service.listMembers(orgId, page, size)));
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
    return Mono.just(HttpResponse.ok(service.listRoles(orgId, page, size)));
  }
}
