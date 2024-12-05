package com.consoleconnect.vortex.gateway.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.gateway.dto.*;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import com.consoleconnect.vortex.gateway.service.PathAccessRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/path-access-rules", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Resource Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtPathAccessRuleController {

  private final PathAccessRuleService service;

  @Operation(summary = "List all existing paths")
  @GetMapping("")
  public Mono<HttpResponse<Paging<PathAccessRule>>> search(
      @RequestParam(value = "method", required = false) String method,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "action", required = false) AccessActionEnum action,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info("search, page:{}, size:{}", page, size);
    return Mono.just(HttpResponse.ok(service.search(method, path, action, page, size)));
  }

  @Operation(summary = "Create a new path access rule")
  @PostMapping("")
  public Mono<HttpResponse<PathAccessRule>> create(
      @Validated @RequestBody CreatePathAccessRuleRequest request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.create(request, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "update a path access rule")
  @PatchMapping("/{id}")
  public Mono<HttpResponse<PathAccessRule>> update(
      @PathVariable String id,
      @Validated @RequestBody UpdatePathAccessRuleRequest request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.update(id, request, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "Retrieve a path access rule by id")
  @GetMapping("/{id}")
  public Mono<HttpResponse<PathAccessRule>> findOne(@PathVariable String id) {
    return Mono.just(HttpResponse.ok(service.findOne(id)));
  }

  @Operation(summary = "Delete a path access rule by id")
  @DeleteMapping("/{id}")
  public Mono<HttpResponse<PathAccessRule>> delete(
      @PathVariable String id, JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.delete(id, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }
}
