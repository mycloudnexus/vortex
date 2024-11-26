package com.consoleconnect.vortex.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.*;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.connections.ConnectionsPage;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.consoleconnect.vortex.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("auth-hs256")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = TestApplication.class)
// @WireMockTest(httpPort = 3031)
@Slf4j
class OrganizationServiceTest extends AbstractIntegrationTest {
  @Autowired OrganizationService organizationService;
  @SpyBean Auth0Client auth0Client;
  @SpyBean EmailService emailService;
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
            updateMetadata.setLoginType(ConnectionStrategyEnum.AUTH0);
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

    Request<Void> addRequest = mock(Request.class);
    doReturn(addRequest).when(organizationsEntity).addConnection(anyString(), any());
    doReturn(mock(Response.class)).when(addRequest).execute();

    ConnectionsEntity connectionsEntity = mock(ConnectionsEntity.class);
    doReturn(connectionsEntity).when(managementAPI).connections();

    Request<ConnectionsPage> connectionsPageRequest = mock(Request.class);
    doReturn(connectionsPageRequest).when(connectionsEntity).listAll(any());

    Response<ConnectionsPage> connectionResponse = mock(Response.class);
    doReturn(connectionResponse).when(connectionsPageRequest).execute();

    ConnectionsPage existConnection =
        new ConnectionsPage(List.of(new Connection("test-auth0", "auth0")));
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
  void testUpdateStatusActiveBranchNotExist() throws Auth0Exception {
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

    ConnectionsEntity connectionsEntity = mock(ConnectionsEntity.class);
    doReturn(connectionsEntity).when(managementAPI).connections();

    Request<ConnectionsPage> connectionsPageRequest = mock(Request.class);
    doReturn(connectionsPageRequest).when(connectionsEntity).listAll(any());

    Response<ConnectionsPage> connectionResponse = mock(Response.class);
    doReturn(connectionResponse).when(connectionsPageRequest).execute();

    ConnectionsPage existConnection = new ConnectionsPage(List.of(new Connection("abc", "samlp")));
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
    metadata.setLoginType(ConnectionStrategyEnum.AUTH0);
    queryOrganization.setMetadata(
        JsonToolkit.fromJson(JsonToolkit.toJson(metadata), new TypeReference<>() {}));
    doReturn(queryOrganization).when(queryResponse).getBody();

    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategyEnum.AUTH0);

    assertThrows(
        Exception.class,
        () ->
            organizationService.createConnection(
                UUID.randomUUID().toString(), createConnectionDto, SYSTEM));
  }

  @Test
  void testSAMLConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        ConnectionStrategyEnum.AUTH0, OrgStatusEnum.ACTIVE, ConnectionStrategyEnum.SAML);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategyEnum.SAML);

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
        ConnectionStrategyEnum.AUTH0, OrgStatusEnum.ACTIVE, ConnectionStrategyEnum.OIDC);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategyEnum.OIDC);

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
        ConnectionStrategyEnum.SAML, OrgStatusEnum.ACTIVE, ConnectionStrategyEnum.AUTH0);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategyEnum.AUTH0);

    OrganizationConnection newOrg =
        organizationService.createConnection(
            UUID.randomUUID().toString(), createConnectionDto, SYSTEM);
    assertNotNull(newOrg);
  }

  @Test
  void testDBConnectionOrgInactive() throws Auth0Exception {
    mockOrgConnectionOperation(
        ConnectionStrategyEnum.SAML, OrgStatusEnum.INACTIVE, ConnectionStrategyEnum.AUTH0);

    // call creating method
    CreateConnectionDto createConnectionDto = new CreateConnectionDto();
    createConnectionDto.setStrategy(ConnectionStrategyEnum.AUTH0);

    assertThrows(
        Exception.class,
        () ->
            organizationService.createConnection(
                UUID.randomUUID().toString(), createConnectionDto, SYSTEM));
  }

  @Test
  void testUpdateSAMLConnection() throws Auth0Exception {
    mockOrgConnectionOperation(
        ConnectionStrategyEnum.SAML, OrgStatusEnum.ACTIVE, ConnectionStrategyEnum.SAML);

    // call creating method
    UpdateConnectionDto request = new UpdateConnectionDto();
    request.setId("con_YNEZH8rgZ8sQz9Fq");
    SamlConnectionDto saml = new SamlConnectionDto();

    request.setSaml(saml);
    OrganizationConnection newOrg =
        organizationService.updateConnection(UUID.randomUUID().toString(), request, SYSTEM);
    assertNotNull(newOrg);
  }

  @Test
  void testCreateInvitationUsername() {
    CreateInvitationDto request = new CreateInvitationDto();
    request.setEmail("test@example.com");
    request.setRoles(List.of("PLATFORM_ADMIN"));

    Auth0Property.Config config = new Auth0Property.Config();
    config.setClientId(UUID.randomUUID().toString());

    String orgId = UUID.randomUUID().toString();
    Auth0Property auth0 = new Auth0Property();
    auth0.setApp(config);
    doReturn(auth0).when(auth0Client).getAuth0Property();
    assertThrows(
        Exception.class, () -> organizationService.createInvitation(orgId, request, SYSTEM));
  }

  @Test
  @SneakyThrows
  void test_listMembers_empty() {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());
    doReturn(membersPageResponse).when(membersPageRequest).execute();
    doReturn(new MembersPage(List.of())).when(membersPageResponse).getBody();

    Paging<MemberInfo> memberPaging =
        organizationService.listMembers(UUID.randomUUID().toString(), 0, PagingHelper.ALL);
    assertNotNull(memberPaging);
    assertEquals(0, memberPaging.getData().size());
  }

  @Test
  @SneakyThrows
  void test_listInvitations() {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    List<Invitation> invitations =
        JsonToolkit.fromJson(
            AbstractIntegrationTest.readFileToString("auth0/organization_invitations.json"),
            new TypeReference<List<Invitation>>() {});

    InvitationsPage membersPage = new InvitationsPage(0, 2, null, 2, invitations);
    Request<InvitationsPage> membersPageRequest = mock(Request.class);
    Response<InvitationsPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageRequest).when(organizationsEntity).getInvitations(anyString(), any());
    doReturn(membersPageResponse).when(membersPageRequest).execute();
    doReturn(membersPage).when(membersPageResponse).getBody();

    Paging<Invitation> invitationPaging =
        organizationService.listInvitations(UUID.randomUUID().toString(), 0, PagingHelper.ALL);
    assertNotNull(invitationPaging);
    assertEquals(2, invitationPaging.getData().size());

    invitationPaging = organizationService.listInvitations(UUID.randomUUID().toString(), 0, 10);
    assertNotNull(invitationPaging);
    assertEquals(2, invitationPaging.getData().size());
  }

  @Test
  @SneakyThrows
  void test_search() {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    String memberPageStr =
        AbstractIntegrationTest.readFileToString("auth0/page_organizations.json");
    List<Organization> organizations =
        JsonToolkit.fromJson(memberPageStr, new TypeReference<List<Organization>>() {});

    OrganizationsPage membersPage = new OrganizationsPage(0, 2, 2, 10, "", organizations);
    Request<OrganizationsPage> membersPageRequest = mock(Request.class);
    Response<OrganizationsPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageRequest).when(organizationsEntity).list(any());
    doReturn(membersPageResponse).when(membersPageRequest).execute();
    doReturn(membersPage).when(membersPageResponse).getBody();
    Paging<Organization> organizationPaging = organizationService.search(0, 2);
    assertNotNull(organizationPaging);
    assertEquals(2, organizationPaging.getData().size());
  }

  private void mockOrgConnectionOperation(
      ConnectionStrategyEnum loginType,
      OrgStatusEnum orgStatusEnum,
      ConnectionStrategyEnum strategryEnum)
      throws Auth0Exception {
    String connectionId = "con_YNEZH8rgZ8sQz9Fq";
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<Organization> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(organizationsEntity).get(anyString());

    Response<Organization> organizationResponse = mock(Response.class);
    doReturn(organizationResponse).when(organizationRequest).execute();

    // mock query organization
    Organization queryOrganization = mock(Organization.class);
    doReturn(queryOrganization).when(organizationResponse).getBody();

    doReturn("test").when(queryOrganization).getDisplayName();
    OrganizationMetadata metadata = new OrganizationMetadata();
    metadata.setStatus(orgStatusEnum);
    metadata.setLoginType(loginType);
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
    doReturn(queryConnection).when(updateResponse).getBody();
  }

  @Test
  void testReset() throws Auth0Exception {
    String userId = UUID.randomUUID().toString();
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    MembersPage membersPage = mock(MembersPage.class);
    doReturn(membersPage).when(membersPageResponse).getBody();

    Member member = mock(Member.class);
    doReturn(List.of(member)).when(membersPage).getItems();
    doReturn(userId).when(member).getUserId();
    doReturn("test@example.com").when(member).getEmail();

    // mock query connection
    mockConnectionForOrg(ConnectionStrategyEnum.AUTH0, organizationsEntity);

    AuthAPI authAPI = mock(AuthAPI.class);
    doReturn(authAPI).when(auth0Client).getAuthClient();

    Request<Void> resetPasswordRequest = mock(Request.class);
    Response<Void> resetPasswordResponse = mock(Response.class);
    doReturn(resetPasswordRequest).when(authAPI).resetPassword(anyString(), any());
    doReturn(resetPasswordResponse).when(resetPasswordRequest).execute();

    assertDoesNotThrow(() -> organizationService.resetPassword(SYSTEM, userId, SYSTEM));
  }

  private static void mockConnectionForOrg(
      ConnectionStrategyEnum strategyEnum, OrganizationsEntity organizationsEntity)
      throws Auth0Exception {
    Request<EnabledConnectionsPage> enabledConnectionsPageRequest = mock(Request.class);
    Response<EnabledConnectionsPage> enabledConnectionsPageResponse = mock(Response.class);
    EnabledConnection enabledConnection = new EnabledConnection("test-connection-xxx");
    com.auth0.json.mgmt.organizations.Connection connection =
        new com.auth0.json.mgmt.organizations.Connection();
    connection.setName("test");
    connection.setStrategy(strategyEnum.getValue());
    enabledConnection.setConnection(connection);
    EnabledConnectionsPage enabledConnectionsPage =
        new EnabledConnectionsPage(List.of(enabledConnection));
    doReturn(enabledConnectionsPageRequest)
        .when(organizationsEntity)
        .getConnections(anyString(), any());
    doReturn(enabledConnectionsPageResponse).when(enabledConnectionsPageRequest).execute();
    doReturn(enabledConnectionsPage).when(enabledConnectionsPageResponse).getBody();
  }

  @Test
  void test_reset_password_exception() throws Auth0Exception {
    String userId = UUID.randomUUID().toString();
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    MembersPage membersPage = mock(MembersPage.class);
    doReturn(membersPage).when(membersPageResponse).getBody();

    Member member = mock(Member.class);
    doReturn(List.of(member)).when(membersPage).getItems();
    doReturn(userId).when(member).getUserId();
    doReturn("test@example.com").when(member).getEmail();

    // mock query connection
    mockConnectionForOrg(ConnectionStrategyEnum.SAML, organizationsEntity);

    AuthAPI authAPI = mock(AuthAPI.class);
    doReturn(authAPI).when(auth0Client).getAuthClient();

    Request<Void> resetPasswordRequest = mock(Request.class);
    Response<Void> resetPasswordResponse = mock(Response.class);
    doReturn(resetPasswordRequest).when(authAPI).resetPassword(anyString(), any());
    doReturn(resetPasswordResponse).when(resetPasswordRequest).execute();

    assertThrows(Exception.class, () -> organizationService.resetPassword(SYSTEM, userId, SYSTEM));
  }

  @Test
  void testResetException() throws Auth0Exception {
    String userId = UUID.randomUUID().toString();
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    MembersPage membersPage = mock(MembersPage.class);
    doReturn(membersPage).when(membersPageResponse).getBody();

    Member member = mock(Member.class);
    doReturn(List.of(member)).when(membersPage).getItems();
    doReturn(userId).when(member).getUserId();
    doReturn("test@example.com").when(member).getEmail();

    assertThrows(
        Exception.class,
        () -> organizationService.resetPassword(SYSTEM, UUID.randomUUID().toString(), SYSTEM));
  }

  @Test
  void testResetException_emptyMember() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    MembersPage membersPage = mock(MembersPage.class);
    doReturn(membersPage).when(membersPageResponse).getBody();

    doReturn(List.of()).when(membersPage).getItems();

    assertThrows(
        Exception.class,
        () -> organizationService.resetPassword(SYSTEM, UUID.randomUUID().toString(), SYSTEM));
  }

  @Test
  void testResetException_emptyMember2() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    assertThrows(
        Exception.class,
        () -> organizationService.resetPassword(SYSTEM, UUID.randomUUID().toString(), SYSTEM));
  }

  @Test
  void testResetException_Auth0Exception() throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    doThrow(Auth0Exception.class).when(membersPageRequest).execute();

    assertThrows(
        Exception.class,
        () -> organizationService.resetPassword(SYSTEM, UUID.randomUUID().toString(), SYSTEM));
  }

  @Test
  void test_revokeInvitation() throws Auth0Exception {
    String userId = UUID.randomUUID().toString();
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    Auth0Property auth0Property = new Auth0Property();
    Auth0Property.Config app = new Auth0Property.Config();
    app.setClientId(UUID.randomUUID().toString());
    auth0Property.setApp(app);
    doReturn(auth0Property).when(auth0Client).getAuth0Property();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Invitation invitation = mock(Invitation.class);
    doReturn(mock(Inviter.class)).when(invitation).getInviter();
    doReturn(mock(Invitee.class)).when(invitation).getInvitee();
    doReturn(mock(Roles.class)).when(invitation).getRoles();

    Request<Invitation> invitationRequest = mock(Request.class);
    Response<Invitation> invitationResponse = mock(Response.class);
    doReturn(invitationRequest)
        .when(organizationsEntity)
        .getInvitation(anyString(), anyString(), any());
    doReturn(invitationResponse).when(invitationRequest).execute();
    doReturn(invitation).when(invitationResponse).getBody();

    Request<Void> existInvitationRequest = mock(Request.class);
    Response<Void> existInvitationResponse = mock(Response.class);
    doReturn(existInvitationRequest).when(organizationsEntity).deleteInvitation(anyString(), any());
    doReturn(existInvitationResponse).when(existInvitationRequest).execute();

    assertDoesNotThrow(() -> organizationService.revokeInvitation(SYSTEM, userId, SYSTEM));
  }

  @Test
  void test_changeStatus() throws Auth0Exception {
    String userId = mockExistedOrgUser(null);
    assertDoesNotThrow(() -> organizationService.changeMemberStatus(SYSTEM, userId, true, SYSTEM));
  }

  @Test
  void test_updateMemberName() throws Auth0Exception {
    String userName = "test-username";
    MemberInfoUpdateDto memberInfoUpdateDto = new MemberInfoUpdateDto();
    String userId = mockExistedOrgUser(userName);
    User user =
        organizationService.updateMemberInfo(SYSTEM, userId, memberInfoUpdateDto, "request-123");
    assertEquals(userName, user.getName());
  }

  private String mockExistedOrgUser(String userName) throws Auth0Exception {
    String userId = UUID.randomUUID().toString();
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    Request<MembersPage> membersPageRequest = mock(Request.class);
    doReturn(membersPageRequest).when(organizationsEntity).getMembers(anyString(), any());

    Response<MembersPage> membersPageResponse = mock(Response.class);
    doReturn(membersPageResponse).when(membersPageRequest).execute();

    MembersPage membersPage = mock(MembersPage.class);
    doReturn(membersPage).when(membersPageResponse).getBody();

    Member member = mock(Member.class);
    doReturn(List.of(member)).when(membersPage).getItems();
    doReturn(userId).when(member).getUserId();
    doReturn("test@example.com").when(member).getEmail();

    UsersEntity usersEntity = mock(UsersEntity.class);
    doReturn(usersEntity).when(managementAPI).users();

    User user = mock(User.class);
    Request<User> userRequest = mock(Request.class);
    Response<User> userResponse = mock(Response.class);
    doReturn(userRequest).when(usersEntity).update(anyString(), any());
    doReturn(userResponse).when(userRequest).execute();
    doReturn(user).when(userResponse).getBody();
    if (StringUtils.isNotBlank(userName)) {
      doReturn(userName).when(user).getName();
    }

    mockConnectionForOrg(ConnectionStrategyEnum.AUTH0, organizationsEntity);
    return userId;
  }

  @Test
  @SneakyThrows
  void test_getOneConnection() {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();

    OrganizationsEntity organizationsEntity = mock(OrganizationsEntity.class);
    doReturn(organizationsEntity).when(managementAPI).organizations();

    EnabledConnection enabledConnection =
        JsonToolkit.fromJson(
            AbstractIntegrationTest.readFileToString("auth0/organization_enabled_connection.json"),
            EnabledConnection.class);
    Request<EnabledConnectionsPage> enabledConnectionsPage = mock(Request.class);
    doReturn(enabledConnectionsPage).when(organizationsEntity).getConnections(anyString(), any());

    Response<EnabledConnectionsPage> enabledConnectionsPageResponse = mock(Response.class);
    doReturn(enabledConnectionsPageResponse).when(enabledConnectionsPage).execute();

    EnabledConnectionsPage connectionsPage = new EnabledConnectionsPage(List.of(enabledConnection));
    doReturn(connectionsPage).when(enabledConnectionsPageResponse).getBody();

    ConnectionsEntity connectionsEntity = mock(ConnectionsEntity.class);
    doReturn(connectionsEntity).when(managementAPI).connections();

    Request<Connection> connectionRequest = mock(Request.class);
    doReturn(connectionRequest).when(connectionsEntity).get(anyString(), any());

    Response<Connection> connectionResponse = mock(Response.class);
    doReturn(connectionResponse).when(connectionRequest).execute();

    Connection connection =
        JsonToolkit.fromJson(
            AbstractIntegrationTest.readFileToString("auth0/organization_connection.json"),
            Connection.class);
    doReturn(connection).when(connectionResponse).getBody();

    OrganizationConnection organizationConnection =
        organizationService.getOneConnection(UUID.randomUUID().toString());
    assertNotNull(organizationConnection);
  }
}
