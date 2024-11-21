package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/mgmt/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Mgmt", description = "Mgmt APIs")
@Slf4j
public class MgmtRoleController {

  @Operation(summary = "List all existing roles")
  @GetMapping("")
  public Mono<HttpResponse<List<RoleEnum>>> search() {
    return Mono.just(HttpResponse.ok(List.of(RoleEnum.PLATFORM_ADMIN, RoleEnum.PLATFORM_MEMBER)));
  }
}
