package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
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
@RequestMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organization", description = "Organization APIs")
@Slf4j
public class OrganizationController {

  private final OrganizationService service;

  @Operation(summary = "List all existing organizations")
  @GetMapping("")
  public HttpResponse<Paging<Organization>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info("search, q:{}, page:{}, size:{}", q, page, size);
    return HttpResponse.ok(service.search(q, page, size));
  }

  @Operation(summary = "Retrieve an organization by id")
  @GetMapping("/{orgId}")
  public HttpResponse<Organization> findOne(@PathVariable String orgId) {
    return HttpResponse.ok(service.findOne(orgId));
  }

  @Operation(summary = "Create a new organization")
  @PostMapping("")
  public HttpResponse<Organization> create(
      @RequestBody CreateOrganizationDto request, JwtAuthenticationToken authenticationToken) {
    return HttpResponse.ok(service.create(request, authenticationToken.getName()));
  }
}
