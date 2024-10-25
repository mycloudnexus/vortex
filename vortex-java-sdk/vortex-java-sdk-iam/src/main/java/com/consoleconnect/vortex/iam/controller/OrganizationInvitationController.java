package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateInivitationDto;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/organizations/{orgId}/invitations",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organization", description = "Organization APIs")
@Slf4j
public class OrganizationInvitationController {

  private final OrganizationService service;

  @Operation(summary = "List all invitations")
  @GetMapping("")
  public HttpResponse<Paging<Invitation>> search(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(service.listInvitations(orgId, page, size));
  }

  @Operation(summary = "Create a new invitation")
  @PostMapping()
  public HttpResponse<Invitation> create(
      @PathVariable String orgId,
      @RequestBody CreateInivitationDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return HttpResponse.ok(
        service.createInvitation(orgId, request, jwtAuthenticationToken.getName()));
  }

  @Operation(summary = "Retrieve an invitation by id")
  @GetMapping("/{invitationId}")
  public HttpResponse<Invitation> findOne(
      @PathVariable String orgId, @PathVariable String invitationId) {
    return HttpResponse.ok(service.getInvitationById(orgId, invitationId));
  }

  @Operation(summary = "Delete an invitation by id")
  @DeleteMapping("/{invitationId}")
  public HttpResponse<Void> delete(
      @PathVariable String orgId,
      @PathVariable String invitationId,
      JwtAuthenticationToken jwtAuthenticationToken) {
    service.deleteInvitation(orgId, invitationId, jwtAuthenticationToken.getName());
    return HttpResponse.ok(null);
  }
}
