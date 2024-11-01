package com.consoleconnect.vortex.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.auth0.client.mgmt.ConnectionsEntity;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.connections.ConnectionsPage;
import com.auth0.json.mgmt.organizations.EnabledConnection;
import com.auth0.json.mgmt.organizations.EnabledConnectionsPage;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.dto.OrganizationMetadata;
import com.consoleconnect.vortex.iam.dto.UpdateOrganizationDto;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class OrganizationServiceTest {
  @Autowired OrganizationService organizationService;
  @SpyBean Auth0Client auth0Client;

  @Test
  void testCreate() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(organizationsEntity).create(any());

    Response<Organization> response = mock(Response.class);
    doReturn(response).when(organizationRequest).execute();

    Organization organization = new Organization("test");
    doReturn(organization).when(response).getBody();

    CreateOrganizationDto request = new CreateOrganizationDto();
    String createdBy = "system";
    request.setName("test");
    request.setDisplayName("test");
    request.setMetadata(new OrganizationMetadata());
    Organization result = organizationService.create(request, createdBy);
    assertEquals(result.getName(), organization.getName());
  }

  @Test
  void testUpdate() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> queryRequest = mock(Request.class);
    doReturn(queryRequest).when(organizationsEntity).get(anyString());

    Response<Organization> queryResponse = mock(Response.class);
    doReturn(queryResponse).when(queryRequest).execute();

    Organization queryOrganization = new Organization("test");
    queryOrganization.setDisplayName("test");
    doReturn(queryOrganization).when(queryResponse).getBody();

    Request<Organization> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(organizationsEntity).update(anyString(), any());

    Response<Organization> response = mock(Response.class);
    doReturn(response).when(organizationRequest).execute();

    Organization organization = new Organization("test");
    organization.setDisplayName("update");
    doReturn(organization).when(response).getBody();

    UpdateOrganizationDto request = new UpdateOrganizationDto();
    String createdBy = "system";
    request.setDisplayName("test");
    Organization result =
        organizationService.update(UUID.randomUUID().toString(), request, createdBy);

    assertNotEquals(result.getDisplayName(), queryOrganization.getDisplayName());
  }

  @Test
  void testUpdateStatusInactive() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> queryRequest = mock(Request.class);
    doReturn(queryRequest).when(organizationsEntity).get(anyString());

    Response<Organization> queryResponse = mock(Response.class);
    doReturn(queryResponse).when(queryRequest).execute();

    Organization queryOrganization = new Organization("test");
    queryOrganization.setDisplayName("test");
    OrganizationMetadata metadata = new OrganizationMetadata();
    metadata.setStatus(OrgStatusEnum.ACTIVE);
    queryOrganization.setMetadata(
        JsonToolkit.fromJson(JsonToolkit.toJson(metadata), new TypeReference<>() {}));
    doReturn(queryOrganization).when(queryResponse).getBody();

    Request<Organization> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(organizationsEntity).update(anyString(), any());
    Response<Organization> updateResponse =
        new Response<Organization>() {
          @Override
          public Map<String, String> getHeaders() {
            return Map.of();
          }

          @Override
          public Organization getBody() {
            Organization updateOrganization = new Organization("test");
            updateOrganization.setDisplayName("test");
            OrganizationMetadata updateMetadata = new OrganizationMetadata();
            updateMetadata.setStatus(OrgStatusEnum.INACTIVE);
            updateOrganization.setMetadata(
                JsonToolkit.fromJson(JsonToolkit.toJson(updateMetadata), new TypeReference<>() {}));
            return updateOrganization;
          }

          @Override
          public int getStatusCode() {
            return HttpStatus.OK.value();
          }
        };
    doReturn(updateResponse).when(organizationRequest).execute();

    Request<EnabledConnectionsPage> enabledConnectionsPage = mock(Request.class);
    doReturn(enabledConnectionsPage).when(organizationsEntity).getConnections(anyString(), any());

    Response<EnabledConnectionsPage> enabledConnectionsPageResponse = mock(Response.class);
    doReturn(enabledConnectionsPageResponse).when(enabledConnectionsPage).execute();

    EnabledConnection enabledConnection = new EnabledConnection("test");
    EnabledConnectionsPage connectionsPage = new EnabledConnectionsPage(List.of(enabledConnection));
    doReturn(connectionsPage).when(enabledConnectionsPageResponse).getBody();

    Request<Void> delRequest = mock(Request.class);
    doReturn(delRequest).when(organizationsEntity).deleteConnection(anyString(), anyString());
    doReturn(mock(Response.class)).when(delRequest).execute();

    String createdBy = "system";
    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.INACTIVE, createdBy);
    OrganizationMetadata resultMetadata =
        JsonToolkit.fromJson(JsonToolkit.toJson(result.getMetadata()), OrganizationMetadata.class);
    assertEquals(OrgStatusEnum.INACTIVE, resultMetadata.getStatus());
  }

  @Test
  void testUpdateStatusActive() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> queryRequest = mock(Request.class);
    doReturn(queryRequest).when(organizationsEntity).get(anyString());

    Response<Organization> queryResponse = mock(Response.class);
    doReturn(queryResponse).when(queryRequest).execute();

    Organization queryOrganization = new Organization("test");
    queryOrganization.setDisplayName("test");
    OrganizationMetadata metadata = new OrganizationMetadata();
    metadata.setStatus(OrgStatusEnum.INACTIVE);
    queryOrganization.setMetadata(
        JsonToolkit.fromJson(JsonToolkit.toJson(metadata), new TypeReference<>() {}));
    doReturn(queryOrganization).when(queryResponse).getBody();

    Request<Organization> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(organizationsEntity).update(anyString(), any());
    Response<Organization> updateResponse =
        new Response<Organization>() {
          @Override
          public Map<String, String> getHeaders() {
            return Map.of();
          }

          @Override
          public Organization getBody() {
            Organization updateOrganization = new Organization("test");
            updateOrganization.setDisplayName("test");
            OrganizationMetadata updateMetadata = new OrganizationMetadata();
            updateMetadata.setStatus(OrgStatusEnum.ACTIVE);
            updateOrganization.setMetadata(
                JsonToolkit.fromJson(JsonToolkit.toJson(updateMetadata), new TypeReference<>() {}));
            return updateOrganization;
          }

          @Override
          public int getStatusCode() {
            return HttpStatus.OK.value();
          }
        };
    doReturn(updateResponse).when(organizationRequest).execute();

    Request<EnabledConnectionsPage> enabledConnectionsPage = mock(Request.class);
    doReturn(enabledConnectionsPage).when(organizationsEntity).getConnections(anyString(), any());

    Response<EnabledConnectionsPage> enabledConnectionsPageResponse = mock(Response.class);
    doReturn(enabledConnectionsPageResponse).when(enabledConnectionsPage).execute();

    EnabledConnection enabledConnection = new EnabledConnection("test");
    EnabledConnectionsPage connectionsPage = new EnabledConnectionsPage(List.of(enabledConnection));
    doReturn(connectionsPage).when(enabledConnectionsPageResponse).getBody();

    Request<Void> delRequest = mock(Request.class);
    doReturn(delRequest).when(organizationsEntity).deleteConnection(anyString(), anyString());
    doReturn(mock(Response.class)).when(delRequest).execute();

    ConnectionsEntity connectionsEntity = mock(ConnectionsEntity.class);
    doReturn(connectionsEntity).when(managementAPI).connections();

    Request<ConnectionsPage> connectionsPageRequest = mock(Request.class);
    doReturn(connectionsPageRequest).when(connectionsEntity).listAll(any());

    Response<ConnectionsPage> connectionResponse = mock(Response.class);
    doReturn(connectionResponse).when(connectionsPageRequest).execute();

    ConnectionsPage existConnection = new ConnectionsPage(List.of(new Connection("test", "auth0")));
    doReturn(existConnection).when(connectionResponse).getBody();

    String createdBy = "system";
    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.ACTIVE, createdBy);
    OrganizationMetadata resultMetadata =
        JsonToolkit.fromJson(JsonToolkit.toJson(result.getMetadata()), OrganizationMetadata.class);
    assertEquals(OrgStatusEnum.ACTIVE, resultMetadata.getStatus());
  }
}
