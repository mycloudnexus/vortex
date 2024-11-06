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
import com.auth0.json.mgmt.organizations.*;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
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
  private static final String SYSTEM = "system";

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
    request.setName("test");
    request.setDisplayName("test");
    request.setMetadata(new OrganizationMetadata());
    Organization result = organizationService.create(request, SYSTEM);
    assertEquals(result.getName(), organization.getName());
  }

  @Test
  void testCreateNoMetadata() throws Auth0Exception {
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
    request.setName("test");
    request.setDisplayName("test");
    Organization result = organizationService.create(request, SYSTEM);
    assertEquals(result.getName(), organization.getName());
  }

  @Test
  void testCreateLengthException() throws Auth0Exception {
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
    request.setName("testtesttesttesttesttesttesttesttesttesttest");
    request.setDisplayName("test");
    assertThrows(VortexException.class, () -> organizationService.create(request, SYSTEM));
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
    request.setDisplayName("test");
    Organization result = organizationService.update(UUID.randomUUID().toString(), request, SYSTEM);

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

    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.INACTIVE, SYSTEM);
    OrganizationMetadata resultMetadata =
        JsonToolkit.fromJson(JsonToolkit.toJson(result.getMetadata()), OrganizationMetadata.class);
    assertEquals(OrgStatusEnum.INACTIVE, resultMetadata.getStatus());
  }

  @Test
  void testUpdateStatusInactiveBranch() throws Auth0Exception {
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

    EnabledConnectionsPage connectionsPage = new EnabledConnectionsPage(List.of());
    doReturn(connectionsPage).when(enabledConnectionsPageResponse).getBody();

    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.INACTIVE, SYSTEM);
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

    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.ACTIVE, SYSTEM);
    OrganizationMetadata resultMetadata =
        JsonToolkit.fromJson(JsonToolkit.toJson(result.getMetadata()), OrganizationMetadata.class);
    assertEquals(OrgStatusEnum.ACTIVE, resultMetadata.getStatus());
  }

  @Test
  void testUpdateStatusActiveBranch() throws Auth0Exception {
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

    ConnectionsPage existConnection = new ConnectionsPage(List.of());
    doReturn(existConnection).when(connectionResponse).getBody();

    Organization result =
        organizationService.updateStatus(
            UUID.randomUUID().toString(), OrgStatusEnum.ACTIVE, SYSTEM);
    OrganizationMetadata resultMetadata =
        JsonToolkit.fromJson(JsonToolkit.toJson(result.getMetadata()), OrganizationMetadata.class);
    assertEquals(OrgStatusEnum.ACTIVE, resultMetadata.getStatus());
  }

  @Test
  void testUpdateStatusSame() throws Auth0Exception {
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

    assertThrows(
        Exception.class,
        () ->
            organizationService.updateStatus(
                UUID.randomUUID().toString(), OrgStatusEnum.INACTIVE, SYSTEM));
  }

  @Test
  void testDBConnectionException() throws Auth0Exception {
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
    metadata.setLoginType(LoginTypeEnum.USERNAME_PASSWORD);
    queryOrganization.setMetadata(
        JsonToolkit.fromJson(JsonToolkit.toJson(metadata), new TypeReference<>() {}));
    doReturn(queryOrganization).when(queryResponse).getBody();

    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategryEnum.AUTH0);

    assertThrows(
        Exception.class,
        () ->
            organizationService.createConnection(
                UUID.randomUUID().toString(), createConnectionDto, SYSTEM));
  }

  @Test
  void testSAMLConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        LoginTypeEnum.USERNAME_PASSWORD, OrgStatusEnum.ACTIVE, ConnectionStrategryEnum.SAML);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategryEnum.SAML);

    SamlConnectionDto saml = new SamlConnectionDto();

    createConnectionDto.setSaml(saml);
    OrganizationConnection newOrg =
        organizationService.createConnection(
            UUID.randomUUID().toString(), createConnectionDto, SYSTEM);
    assertNotNull(newOrg);
  }

  @Test
  void testOIDCConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        LoginTypeEnum.USERNAME_PASSWORD, OrgStatusEnum.ACTIVE, ConnectionStrategryEnum.OIDC);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategryEnum.OIDC);

    OidcConnectionDto oidc = new OidcConnectionDto();
    oidc.setClientId(UUID.randomUUID().toString());
    createConnectionDto.setOidc(oidc);

    OrganizationConnection newOrg =
        organizationService.createConnection(
            UUID.randomUUID().toString(), createConnectionDto, SYSTEM);
    assertNotNull(newOrg);
  }

  @Test
  void testDBConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        LoginTypeEnum.SSO, OrgStatusEnum.ACTIVE, ConnectionStrategryEnum.AUTH0);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategryEnum.AUTH0);

    OrganizationConnection newOrg =
        organizationService.createConnection(
            UUID.randomUUID().toString(), createConnectionDto, SYSTEM);
    assertNotNull(newOrg);
  }

  @Test
  void testDBConnectionOrgInactive() throws Auth0Exception {
    mockOrgConnectionOperation(
        LoginTypeEnum.SSO, OrgStatusEnum.INACTIVE, ConnectionStrategryEnum.AUTH0);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategryEnum.AUTH0);

    assertThrows(
        Exception.class,
        () ->
            organizationService.createConnection(
                UUID.randomUUID().toString(), createConnectionDto, SYSTEM));
  }

  @Test
  void testUpdateSAMLConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        LoginTypeEnum.SSO, OrgStatusEnum.ACTIVE, ConnectionStrategryEnum.SAML);

    // call creating method
    UpdateConnectionDto request = new UpdateConnectionDto();
    request.setId("con_YNEZH8rgZ8sQz9Fq");
    SamlConnectionDto saml = new SamlConnectionDto();

    request.setSaml(saml);
    OrganizationConnection newOrg =
        organizationService.updateConnection(UUID.randomUUID().toString(), request, SYSTEM);
    assertNotNull(newOrg);
  }

  private void mockOrgConnectionOperation(
      LoginTypeEnum loginTypeEnum,
      OrgStatusEnum orgStatusEnum,
      ConnectionStrategryEnum strategryEnum)
      throws Auth0Exception {
    String connectionId = "con_YNEZH8rgZ8sQz9Fq";
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> queryRequest = mock(Request.class);
    doReturn(queryRequest).when(organizationsEntity).get(anyString());

    Response<Organization> queryResponse = mock(Response.class);
    doReturn(queryResponse).when(queryRequest).execute();

    // mock query organization
    Organization queryOrganization = mock(Organization.class);
    doReturn(queryOrganization).when(queryResponse).getBody();

    doReturn("test").when(queryOrganization).getDisplayName();
    OrganizationMetadata metadata = new OrganizationMetadata();
    metadata.setStatus(orgStatusEnum);
    metadata.setLoginType(loginTypeEnum);
    doReturn(JsonToolkit.fromJson(JsonToolkit.toJson(metadata), new TypeReference<>() {}))
        .when(queryOrganization)
        .getMetadata();
    doReturn(UUID.randomUUID().toString()).when(queryOrganization).getId();

    // mock query members
    MembersPage membersPage = mock(MembersPage.class);
    Request<MembersPage> membersPageRequest = mock(Request.class);
    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());
    doReturn(membersPageResponse).when(membersPageRequest).execute();
    doReturn(membersPage).when(membersPageResponse).getBody();

    // mock query invitations
    InvitationsPage invitationsPage = mock(InvitationsPage.class);
    Request<InvitationsPage> invitationsPageRequest = mock(Request.class);
    Response<InvitationsPage> invitationsPageResponse = mock(Response.class);
    doReturn(invitationsPageRequest).when(organizationsEntity).getInvitations(anyString(), any());
    doReturn(invitationsPageResponse).when(invitationsPageRequest).execute();
    doReturn(invitationsPage).when(invitationsPageResponse).getBody();

    // mock query connection
    Request<EnabledConnectionsPage> enabledConnectionsPageRequest = mock(Request.class);
    Response<EnabledConnectionsPage> enabledConnectionsPageResponse = mock(Response.class);
    EnabledConnection enabledConnection = new EnabledConnection(connectionId);
    EnabledConnectionsPage enabledConnectionsPage =
        new EnabledConnectionsPage(List.of(enabledConnection));
    doReturn(enabledConnectionsPageRequest)
        .when(organizationsEntity)
        .getConnections(anyString(), any());
    doReturn(enabledConnectionsPageResponse).when(enabledConnectionsPageRequest).execute();
    doReturn(enabledConnectionsPage).when(enabledConnectionsPageResponse).getBody();

    // mock add connection
    Request<EnabledConnection> addConnectionRequest = mock(Request.class);
    Response<EnabledConnection> addConnectionResponse = mock(Response.class);
    doReturn(addConnectionRequest).when(organizationsEntity).addConnection(anyString(), any());
    doReturn(addConnectionResponse).when(addConnectionRequest).execute();
    doReturn(enabledConnection).when(addConnectionResponse).getBody();

    // mock delete old connection
    ConnectionsEntity connectionsEntity = mock(ConnectionsEntity.class);
    Request<Void> deleteRequest = mock(Request.class);
    Response<Void> deleteResponse = mock(Response.class);
    doReturn(connectionsEntity).when(managementAPI).connections();
    doReturn(deleteRequest).when(connectionsEntity).delete(anyString());
    doReturn(deleteResponse).when(deleteRequest).execute();

    // mock query a connection
    Connection queryConnection = mock(Connection.class);
    Request<Connection> queryConnectionRequest = mock(Request.class);
    Response<Connection> queryConnectionResponse = mock(Response.class);
    doReturn(queryConnectionRequest).when(connectionsEntity).get(anyString(), any());
    doReturn(queryConnectionResponse).when(queryConnectionRequest).execute();

    // mock create a connection
    doReturn(queryConnectionRequest).when(connectionsEntity).create(any());
    doReturn(queryConnection).when(queryConnectionResponse).getBody();

    // mock update a connection
    doReturn(queryConnectionRequest).when(connectionsEntity).update(anyString(), any());
    doReturn(queryConnection).when(queryConnectionResponse).getBody();

    doReturn(connectionId).when(queryConnection).getId();
    doReturn(strategryEnum.getValue()).when(queryConnection).getStrategy();

    // mock bind new connection
    Request<Organization> updateRequest = mock(Request.class);
    Response<Void> updateResponse = mock(Response.class);
    doReturn(updateRequest).when(organizationsEntity).update(anyString(), any());
    doReturn(updateResponse).when(updateRequest).execute();
  }
}
