package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.dto.DashboardOrganizationInfo;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Dashboard Mgmt", description = "Mgmt dashboard APIs")
@Slf4j
public class MgmtDashboardController {
  private final OrganizationService service;

  @Operation(summary = "List all existing organizations and billing date.")
  @GetMapping("/organizations")
  public Mono<HttpResponse<List<DashboardOrganizationInfo>>> organizations() {
    return Mono.just(HttpResponse.ok(service.search()));
  }
}
