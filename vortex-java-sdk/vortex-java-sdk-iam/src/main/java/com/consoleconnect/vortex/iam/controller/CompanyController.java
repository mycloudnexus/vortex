package com.consoleconnect.vortex.iam.controller;

import com.consoleconnect.vortex.core.entity.CompanyEntity;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.model.req.CompanyDto;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Company Mgmt", description = "Company Mgmt APIs")
@RestController()
@RequestMapping(value = "/companies", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyController {

  private final CompanyService companyService;

  public CompanyController(CompanyService companyService) {
    this.companyService = companyService;
  }

  @Operation(description = "list companies", summary = "list companies")
  @GetMapping()
  public HttpResponse<Paging<CompanyEntity>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(companyService.search(q, page, size));
  }

  @Operation(description = "get one company", summary = "get one company")
  @GetMapping("/{companyId}")
  public HttpResponse<CompanyEntity> getOne(@PathVariable(value = "companyId") String companyId) {
    return HttpResponse.ok(companyService.getOne(companyId));
  }

  @Operation(description = "create company", summary = "create company")
  @PostMapping()
  public HttpResponse<CompanyEntity> create(@RequestBody CompanyDto companyDto) {
    return HttpResponse.ok(companyService.create(companyDto));
  }

  @Operation(description = "update company", summary = "update company")
  @PatchMapping("/{companyId}")
  public HttpResponse<CompanyEntity> update(
      @PathVariable(value = "companyId") String companyId, @RequestBody CompanyDto companyDto) {
    return HttpResponse.ok(companyService.update(companyId, companyDto));
  }

  @Operation(description = "delete company", summary = "delete company")
  @DeleteMapping("/{companyId}")
  public HttpResponse<Boolean> delete(@PathVariable(value = "companyId") String companyId) {
    return HttpResponse.ok(companyService.delete(companyId));
  }
}
