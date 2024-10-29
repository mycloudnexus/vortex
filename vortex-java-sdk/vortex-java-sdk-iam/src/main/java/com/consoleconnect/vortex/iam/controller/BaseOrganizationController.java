package com.consoleconnect.vortex.iam.controller;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Member;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class BaseOrganizationController {

  private final OrganizationService service;

  public Mono<HttpResponse<Paging<Organization>>> search(String q, int page, int size) {
    log.info("search, q:{}, page:{}, size:{}", q, page, size);
    return Mono.just(HttpResponse.ok(service.search(q, page, size)));
  }

  public Mono<HttpResponse<Organization>> create(
      CreateOrganizationDto request, JwtAuthenticationToken authenticationToken) {
    return Mono.just(HttpResponse.ok(service.create(request, authenticationToken.getName())));
  }

  public Mono<HttpResponse<Organization>> update(
      String orgId, UpdateOrganizationDto request, JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.update(orgId, request, authenticationToken.getName())));
  }

  public Mono<HttpResponse<Organization>> findOne(String orgId) {
    return Mono.just(HttpResponse.ok(service.findOne(orgId)));
  }

  public Mono<HttpResponse<Paging<OrganizationConnection>>> listConnections(
      String orgId, int page, int size) {
    return Mono.just(HttpResponse.ok(service.listConnections(orgId, page, size)));
  }

  public Mono<HttpResponse<OrganizationConnection>> createConnection(
      String orgId, CreateConnectionDto request, JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(service.createConnection(orgId, request, authenticationToken.getName())));
  }

  public Mono<HttpResponse<Paging<Invitation>>> listInvitations(String orgId, int page, int size) {
    return Mono.just(HttpResponse.ok(service.listInvitations(orgId, page, size)));
  }

  public Mono<HttpResponse<Invitation>> create(
      @PathVariable String orgId,
      @RequestBody CreateInivitationDto request,
      JwtAuthenticationToken jwtAuthenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.createInvitation(orgId, request, jwtAuthenticationToken.getName())));
  }

  public Mono<HttpResponse<Invitation>> findOne(String orgId, String invitationId) {
    return Mono.just(HttpResponse.ok(service.getInvitationById(orgId, invitationId)));
  }

  public Mono<HttpResponse<Void>> delete(
      String orgId, String invitationId, JwtAuthenticationToken jwtAuthenticationToken) {
    service.deleteInvitation(orgId, invitationId, jwtAuthenticationToken.getName());
    return Mono.just(HttpResponse.ok(null));
  }

  public Mono<HttpResponse<Paging<Member>>> listMembers(String orgId, int page, int size) {
    return Mono.just(HttpResponse.ok(service.listMembers(orgId, page, size)));
  }

  public Mono<HttpResponse<Paging<Role>>> listRoles(String orgId, int page, int size) {
    return Mono.just(HttpResponse.ok(service.listRoles(orgId, page, size)));
  }

  public Mono<HttpResponse<OrganizationConnection>> dbConnection(
      String orgId, JwtAuthenticationToken authenticationToken) {
    return Mono.just(HttpResponse.ok(service.dbConnection(orgId, authenticationToken.getName())));
  }

  public Mono<HttpResponse<OrganizationConnection>> samlConnection(
      String orgId, SamlConnection samlConnection, JwtAuthenticationToken authenticationToken) {
    return Mono.just(
        HttpResponse.ok(
            service.createOrUpdateSAML(orgId, samlConnection, authenticationToken.getName())));
  }
}
