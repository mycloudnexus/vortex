package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.EnabledConnection;
import com.auth0.json.mgmt.organizations.EnabledConnectionsPage;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.OrganizationConnection;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.service.ConnectionService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public abstract class AbstractConnection {
  private final Auth0Client auth0Client;
  protected static final String META_LOGIN_TYPE = "loginType";
  protected static final String META_STATUS = "status";
  private final ConnectionService connectionService;

  protected AbstractConnection(Auth0Client auth0Client, ConnectionService connectionService) {
    this.auth0Client = auth0Client;
    this.connectionService = connectionService;
  }

  public OrganizationConnection createConnection(
      String orgId, CreateConnectionDto createConnectionDto) {
    try {
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      Organization organization = managementAPI.organizations().get(orgId).execute().getBody();

      OrganizationsEntity organizationsEntity = managementAPI.organizations();

      // 1. check organization's status
      validateStatus(orgId, organization);

      // 2. check login type.
      validateLoginType(organization);

      // 3. check and clean existed members and connection.
      checkExistedConnectionAndClean(orgId, organizationsEntity, organization, managementAPI);

      // 4. build a connection instance
      Connection createdConnection =
          buildNewConnection(organization, createConnectionDto, managementAPI);

      // 5. bind a connection to an organization
      return bindOrganizationConnection(
          orgId, createdConnection, organizationsEntity, LoginTypeEnum.SSO);
    } catch (Exception e) {
      log.error("create.connection error", e);
      throw VortexException.badRequest("Create connection error" + e.getMessage());
    }
  }

  private static void validateStatus(String orgId, Organization organization) {
    Map<String, Object> metadata =
        organization.getMetadata() == null ? new HashMap<>() : organization.getMetadata();
    String status = MapUtils.getString(metadata, META_STATUS);
    if (StringUtils.isNotBlank(status) && OrgStatusEnum.INACTIVE.name().equals(status)) {
      throw VortexException.badRequest("This organization is inactive, orgId:" + orgId);
    }
  }

  public OrganizationConnection updateConnection(
      String orgId, UpdateConnectionDto request, String requestedBy) {
    try {
      log.info("updateConnection, orgId:{}, request:{}, requestBy:{}", orgId, request, requestedBy);
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Organization organization = organizationsEntity.get(orgId).execute().getBody();

      validateStatus(orgId, organization);

      canUpateOnLoginType(orgId, organization);

      EnabledConnectionsPage enabledConnectionsPage =
          managementAPI.organizations().getConnections(orgId, null).execute().getBody();
      if (Objects.isNull(enabledConnectionsPage)
          || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
        throw VortexException.badRequest("Don't has a bound connection.");
      }

      EnabledConnection enabledConnection = enabledConnectionsPage.getItems().get(0);
      if (!enabledConnection.getConnectionId().equals(request.getId())) {
        throw VortexException.badRequest("The connection is not bound with organization:" + orgId);
      }

      Request<Connection> response = managementAPI.connections().get(request.getId(), null);
      Connection connection = response.execute().getBody();

      Connection update = buildUpdateConnection(organization, connection, request, managementAPI);

      Connection updatedConnection =
          managementAPI.connections().update(connection.getId(), update).execute().getBody();

      return getOrganizationConnection(connection.getId(), enabledConnection, updatedConnection);
    } catch (Auth0Exception e) {
      log.error("update saml connection error", e);
      throw VortexException.badRequest("Update saml connection error" + e.getMessage());
    }
  }

  void canUpateOnLoginType(String orgId, Organization organization) {
    if (Objects.nonNull(organization.getMetadata())
        && !LoginTypeEnum.SSO.name().equals(organization.getMetadata().get(META_LOGIN_TYPE))) {
      throw VortexException.internalError(
          "Failed to change saml connections of organization: " + orgId);
    }
  }

  abstract Connection buildNewConnection(
      Organization organization,
      CreateConnectionDto createConnectionDto,
      ManagementAPI managementAPI);

  Connection buildUpdateConnection(
      Organization organization,
      Connection connection,
      UpdateConnectionDto updateConnectionDto,
      ManagementAPI managementAPI) {
    throw VortexException.badRequest("Don't support update.");
  }

  public void validateLoginType(Organization organization) {
    if (Objects.nonNull(organization.getMetadata())
        && LoginTypeEnum.SSO.name().equals(organization.getMetadata().get(META_LOGIN_TYPE))) {
      log.warn("current login type is:{}", organization.getMetadata().get(META_LOGIN_TYPE));
      throw VortexException.internalError(
          "Failed to create connections of organization: " + organization.getId());
    }
  }

  private void checkExistedConnectionAndClean(
      String orgId,
      OrganizationsEntity organizationsEntity,
      Organization organization,
      ManagementAPI managementAPI)
      throws Auth0Exception {
    EnabledConnectionsPage enabledConnectionsPage =
        organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
    if (Objects.isNull(enabledConnectionsPage)
        || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
      return;
    }

    if (enabledConnectionsPage.getItems().size() > 1) {
      throw VortexException.internalError(
          "There are more than one connection of organization:" + orgId);
    }
    connectionService.cleanConnectionAndMembers(
        managementAPI, organizationsEntity, enabledConnectionsPage.getItems().get(0), orgId);
  }

  private static OrganizationConnection bindOrganizationConnection(
      String orgId,
      Connection createdConnection,
      OrganizationsEntity organizationsEntity,
      LoginTypeEnum loginTypeEnum)
      throws Auth0Exception {

    // bind connection
    EnabledConnection enabledConnection = new EnabledConnection();
    enabledConnection.setConnectionId(createdConnection.getId());
    enabledConnection.setShowAsButton(true);
    enabledConnection.setAssignMembershipOnLogin(true);

    EnabledConnection createdEnabledConnection =
        organizationsEntity.addConnection(orgId, enabledConnection).execute().getBody();

    Organization organization = organizationsEntity.get(orgId).execute().getBody();
    Map<String, Object> meta =
        organization.getMetadata() == null ? new HashMap<>() : organization.getMetadata();
    meta.put(META_LOGIN_TYPE, loginTypeEnum.name());
    Organization updateMetadata = new Organization();
    updateMetadata.setMetadata(meta);
    organizationsEntity.update(orgId, updateMetadata).execute();

    return getOrganizationConnection(
        createdEnabledConnection.getConnectionId(), createdEnabledConnection, createdConnection);
  }

  private static OrganizationConnection getOrganizationConnection(
      String connection, EnabledConnection enabledConnection, Connection updatedConnection) {
    OrganizationConnection organizationConnection = new OrganizationConnection();
    organizationConnection.setConnectionId(connection);
    organizationConnection.setAssignMembershipOnLogin(
        enabledConnection.isAssignMembershipOnLogin());
    organizationConnection.setShowAsButton(enabledConnection.getShowAsButton());
    organizationConnection.setConnection(updatedConnection);
    return organizationConnection;
  }
}
