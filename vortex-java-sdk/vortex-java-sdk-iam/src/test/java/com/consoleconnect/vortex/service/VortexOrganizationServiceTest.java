package com.consoleconnect.vortex.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.core.entity.OrganizationEntity;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.consoleconnect.vortex.iam.service.VortexOrganizationService;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VortexOrganizationServiceTest extends AbstractIntegrationTest {

  @SpyBean private OrganizationService organizationService;

  @Autowired private VortexOrganizationService vortexOrganizationService;

  private static String orgId;

  @Order(1)
  @Test
  void testCreateOrganization() {
    Organization organization = new Organization();
    doReturn(organization).when(organizationService).create(any(), anyString());

    CreateOrganizationDto req = new CreateOrganizationDto();
    req.setDisplayName("name");
    req.setName("shortname");

    OrganizationEntity org = vortexOrganizationService.create(req, "unit-test");
    Assertions.assertNotNull(org);
    orgId = org.getId().toString();
  }

  @Order(2)
  @Test
  void testUpdateOrganization() {
    CreateOrganizationDto req = new CreateOrganizationDto();
    req.setDisplayName("name-update");

    OrganizationEntity org = vortexOrganizationService.update(orgId, req);
    Assertions.assertEquals(org.getDisplayName(), req.getDisplayName());
  }

  @Order(3)
  @Test
  void testListCompanies() {
    Paging<OrganizationEntity> res = vortexOrganizationService.search(null, null, 0, 1);
    Assertions.assertEquals(1L, res.getTotal());
  }

  @Order(4)
  @Test
  void testGetOrganization() {
    OrganizationEntity org = vortexOrganizationService.findOne(orgId);
    Assertions.assertEquals(org.getId().toString(), orgId);
  }
}
