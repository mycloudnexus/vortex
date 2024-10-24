package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.filter.ConnectionFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.connections.ConnectionsPage;
import com.auth0.json.mgmt.organizations.EnabledConnection;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.client.Auth0Client;
import com.consoleconnect.vortex.iam.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrganizationService {
  private Auth0Client auth0Client;

  @Autowired
  public OrganizationService(Auth0Client auth0Client) {
    this.auth0Client = auth0Client;
  }

  public Organization getByName(String shortName) {
    OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
    Request<Organization> organizationRequest = organizationsEntity.getByName(shortName);
    try {
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("[module-auth]getOrganizations.error", e);
      throw VortexException.badRequest("[module-auth]getOrganizations.error" + e.getMessage());
    }
  }

  public Organization update(String shortName, String displayName) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Organization> organizationFindRequest = organizationsEntity.getByName(shortName);
      Organization organization = organizationFindRequest.execute().getBody();

      Organization modify = new Organization();
      modify.setDisplayName(displayName);
      Request<Organization> organizationRequest =
          organizationsEntity.update(organization.getId(), modify);
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("[module-auth]update organizations.error", e);
      throw VortexException.badRequest("[module-auth]update organizations.error" + e.getMessage());
    }
  }

  public Organization create(String identifier, String displayName) {
    OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
    Organization organization = new Organization(identifier);
    organization.setDisplayName(displayName);
    Request<Organization> organizationRequest = organizationsEntity.create(organization);
    try {
      organization = organizationRequest.execute().getBody();

      // add default connection: username-and-password
      EnabledConnection enabledConnection = new EnabledConnection(getDefaultConnection().getId());
      Request<EnabledConnection> enabledConnectionRequest =
          organizationsEntity.addConnection(organization.getId(), enabledConnection);
      enabledConnectionRequest.execute().getBody();

      return organization;
    } catch (Auth0Exception e) {
      log.error("create organizations.error", e);
      throw VortexException.badRequest("[module-auth]create organizations.error" + e.getMessage());
    }
  }

  public Connection getDefaultConnection() {
    try {
      ConnectionFilter filter = new ConnectionFilter();
      filter.withStrategy(Constants.DEFAULT_CONNECTION_STRATEGY);
      filter.withName(Constants.DEFAULT_CONNECTION_NAME);
      ConnectionsPage response =
          this.auth0Client.getMgmtClient().connections().listAll(filter).execute().getBody();
      return response.getItems().get(0);
    } catch (Exception e) {
      log.error("get.connection error", e);
      throw VortexException.badRequest("Get the default connection error," + e.getMessage());
    }
  }

  public EnabledConnection bindConnection(String orgName, String connectionId) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Organization> organizationRequest = organizationsEntity.getByName(orgName);
      Organization organization = organizationRequest.execute().getBody();

      EnabledConnection enabledConnection = new EnabledConnection(connectionId);
      enabledConnection.setAssignMembershipOnLogin(false);
      enabledConnection.setShowAsButton(false);

      Request<EnabledConnection> enabledConnectionRequest =
          organizationsEntity.addConnection(organization.getId(), enabledConnection);
      return enabledConnectionRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("[module-auth]bind connection.error", e);
      throw VortexException.badRequest("[module-auth]bind connection.error" + e.getMessage());
    }
  }

  public int delete(String shortName) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Organization> organizationFindRequest = organizationsEntity.getByName(shortName);
      Organization organization = organizationFindRequest.execute().getBody();

      Request<Void> organizationRequest = organizationsEntity.delete(organization.getId());
      return organizationRequest.execute().getStatusCode();
    } catch (Auth0Exception e) {
      log.error("[module-auth]delete organizations.error", e);
      throw VortexException.badRequest("[module-auth]delete organizations.error" + e.getMessage());
    }
  }
}
