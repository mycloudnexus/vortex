package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.OrganizationConnection;
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
    value = "/organizations/{orgId}/connections",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organization", description = "Organization APIs")
@Slf4j
public class OrganizationConnectionController {

  private final OrganizationService service;

  @Operation(summary = "List all existing connections")
  @GetMapping("")
  public HttpResponse<Paging<OrganizationConnection>> search(
      @PathVariable String orgId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(service.listConnections(orgId, page, size));
  }

  @Operation(summary = "Setup a connection")
  @PostMapping("")
  public HttpResponse<OrganizationConnection> create(
      @PathVariable String orgId,
      @RequestBody CreateConnectionDto request,
      JwtAuthenticationToken authenticationToken) {
    return HttpResponse.ok(service.createConnection(orgId, request, authenticationToken.getName()));
  }
}
