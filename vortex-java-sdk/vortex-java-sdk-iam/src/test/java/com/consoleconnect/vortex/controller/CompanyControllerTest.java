package com.consoleconnect.vortex.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.core.entity.CompanyEntity;
import com.consoleconnect.vortex.core.model.HttpResponse;
import com.consoleconnect.vortex.core.model.req.CompanyDto;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import com.consoleconnect.vortex.test.WebTestClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyControllerTest extends AbstractIntegrationTest {

  @SpyBean private OrganizationService organizationService;

  @Autowired WebTestClient webTestClient;

  private static final String COMPANY_BASE_PATH = "/companies";

  private final WebTestClientHelper testClientHelper;

  @Autowired
  CompanyControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  private static String companyId;

  @Order(1)
  @Test
  void testCreateCompany() {
    Organization organization = new Organization();
    doReturn(organization).when(organizationService).create(anyString(), anyString());

    CompanyDto req = new CompanyDto();
    req.setCompanyName("name");
    req.setShortName("shortname");

    String path = String.format("%s", COMPANY_BASE_PATH);
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        req,
        new ParameterizedTypeReference<HttpResponse<CompanyEntity>>() {},
        res -> {
          Assertions.assertNotNull(res.getData());
          companyId = res.getData().getId().toString();
        });
  }

  @Order(2)
  @Test
  void testUpdateCompany() {
    Organization organization = new Organization();
    doReturn(organization).when(organizationService).update(anyString(), anyString());

    CompanyDto req = new CompanyDto();
    req.setCompanyName("name-update");

    String path = String.format("%s/%s", COMPANY_BASE_PATH, companyId);
    testClientHelper.patchAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        req,
        new ParameterizedTypeReference<HttpResponse<CompanyEntity>>() {},
        res -> {
          Assertions.assertEquals(res.getData().getCompanyName(), req.getCompanyName());
        });
  }

  @Order(3)
  @Test
  void testListCompanies() {
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(COMPANY_BASE_PATH).build()),
        new ParameterizedTypeReference<HttpResponse<Paging<CompanyEntity>>>() {},
        res -> {
          Assertions.assertEquals(res.getData().getTotal(), 2L);
        });
  }

  @Order(4)
  @Test
  void testGetCompany() {
    String path = String.format("%s/%s", COMPANY_BASE_PATH, companyId);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        new ParameterizedTypeReference<HttpResponse<CompanyEntity>>() {},
        res -> {
          Assertions.assertEquals(res.getData().getId().toString(), companyId);
        });
  }

  @Order(5)
  @Test
  void testDeleteCompany() {
    doReturn(1).when(organizationService).delete(anyString());

    String path = String.format("%s/%s", COMPANY_BASE_PATH, companyId);
    testClientHelper.deleteAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        new ParameterizedTypeReference<HttpResponse<Boolean>>() {},
        res -> {
          Assertions.assertTrue(res.getData());
        });
  }
}
