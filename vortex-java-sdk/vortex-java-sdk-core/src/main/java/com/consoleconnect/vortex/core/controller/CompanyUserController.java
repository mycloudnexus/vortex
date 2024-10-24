package com.consoleconnect.vortex.core.controller;

import com.consoleconnect.vortex.auth.model.UserResponse;
import com.consoleconnect.vortex.auth.model.UserSignUpReq;
import com.consoleconnect.vortex.auth.service.OrganizationUserService;
import com.consoleconnect.vortex.core.entity.CompanyEntity;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Company user mgmt", description = "Company user mgmt APIs")
@RestController()
@RequestMapping(value = "/company-users", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyUserController {
  private final CompanyService companyService;
  private final OrganizationUserService organizationUserService;

  public CompanyUserController(
      CompanyService companyService, OrganizationUserService organizationUserService) {
    this.companyService = companyService;
    this.organizationUserService = organizationUserService;
  }

  @Operation(description = "List company users", summary = "List company users")
  @GetMapping("/{companyId}")
  public HttpResponse<List<UserResponse>> list(
      @PathVariable(value = "companyId") String companyId) {
    CompanyEntity company = companyService.getOne(companyId);
    return HttpResponse.ok(organizationUserService.listByOrg(company.getShortName()));
  }

  @Operation(description = "create company", summary = "create company")
  @PostMapping()
  public HttpResponse<String> signUp(@Valid @RequestBody UserSignUpReq userSignUpReq) {
    companyService.getOneByShortName(userSignUpReq.getOrganizationName());
    return HttpResponse.ok(organizationUserService.signUp(userSignUpReq));
  }
}
