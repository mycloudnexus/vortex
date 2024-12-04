package com.consoleconnect.vortex.gateway.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.dto.Resource;
import com.consoleconnect.vortex.gateway.dto.UpdateResourceRequest;
import com.consoleconnect.vortex.gateway.service.ResourceService;
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
@RequestMapping(value = "/mgmt/resources", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Resource Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtResourceController {

  private final ResourceService service;

  @Operation(summary = "List all existing resources")
  @GetMapping("")
  public Mono<HttpResponse<Paging<Resource>>> search(
      @RequestParam(value = "customerId", required = false) String customerId,
      @RequestParam(value = "resourceType", required = false) String resourceType,
      @RequestParam(value = "orderId", required = false) String orderId,
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info("search, page:{}, size:{}", page, size);
    return Mono.just(
        HttpResponse.ok(service.search(customerId, resourceType, orderId, resourceId, page, size)));
  }

  @Operation(summary = "Create a new resource")
  @PostMapping("")
  public Mono<HttpResponse<Resource>> create(
      @Validated @RequestBody CreateResourceRequest request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.create(request, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "update a resource")
  @PatchMapping("/{id}")
  public Mono<HttpResponse<Resource>> update(
      @PathVariable String id,
      @Validated @RequestBody UpdateResourceRequest request,
      JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.update(id, request, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "Retrieve a resource by id")
  @GetMapping("/{id}")
  public Mono<HttpResponse<Resource>> findOne(@PathVariable String id) {
    return Mono.just(HttpResponse.ok(service.findOne(id)));
  }

  @Operation(summary = "Delete a resource by id")
  @DeleteMapping("/{id}")
  public Mono<HttpResponse<Resource>> delete(
      @PathVariable String id, JwtAuthenticationToken authenticationToken) {
    return Mono.fromCallable(() -> service.delete(id, authenticationToken.getName()))
        .map(HttpResponse::ok)
        .subscribeOn(Schedulers.boundedElastic());
  }
}
