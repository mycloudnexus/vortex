package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ConnectionsEntity;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.core.toolkit.PatternHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class OrganizationService {

  private final Auth0Client auth0Client;
  private final EmailService emailService;

  public Organization create(CreateOrganizationDto request, String createdBy) {
    log.info("creating organization: {},requestedBy:{}", request, createdBy);
    try {
      check(request);
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = new Organization(request.getName());
      organization.setDisplayName(request.getDisplayName());

      if (request.getMetadata() != null) {
        Map<String, Object> metadata =
            JsonToolkit.fromJson(
                JsonToolkit.toJson(request.getMetadata()), new TypeReference<>() {});
        organization.setMetadata(metadata);
      }

      Request<Organization> organizationRequest = organizationsEntity.create(organization);
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("create organizations.error", e);
      throw VortexException.badRequest("create organizations.error" + e.getMessage());
    }
  }

  private void check(CreateOrganizationDto request) {
    if (request == null
        || StringUtils.isBlank(request.getDisplayName())
        || StringUtils.isBlank(request.getName())) {
      throw VortexException.badRequest("Invalid parameters.");
    }

    if (request.getDisplayName().length() > 255) {
      throw VortexException.badRequest("Display name cannot exceed 255 characters.");
    }

    if (request.getName().length() > 20) {
      throw VortexException.badRequest("Name cannot exceed 20 characters.");
    }

    if (!PatternHelper.validShortName(request.getName())) {
      throw VortexException.badRequest("Invalid name.");
    }
  }

  public Organization update(String orgId, UpdateOrganizationDto request, String createdBy) {
    log.info("updating organization: {},{},requestedBy:{}", orgId, request, createdBy);
    if (request == null) {
      throw VortexException.badRequest("Payload cannot be empty.");
    }
    try {
      Organization organization = findOne(orgId);

      if (StringUtils.isNotBlank(request.getDisplayName())) {
        if (request.getDisplayName().length() > 255) {
          throw VortexException.badRequest("Display name cannot exceed 255 characters.");
        }
        organization.setDisplayName(request.getDisplayName());
      }

      if (request.getStatus() != null) {
        Map<String, Object> metadata = organization.getMetadata();
        if (metadata == null) {
          metadata = new HashMap<>();
          organization.setMetadata(metadata);
        }
        metadata.put("status", request.getStatus());
      }
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Organization> organizationRequest = organizationsEntity.update(orgId, organization);
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("update organizations.error", e);
      throw VortexException.badRequest("update organizations.error" + e.getMessage());
    }
  }

  public Paging<Organization> search(String q, int page, int size) {
    log.info("search organizations, q:{}, page:{}, size:{}", q, page, size);
    try {
      PageFilter pageFilter = new PageFilter();
      pageFilter.withTotals(true);
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<OrganizationsPage> organizationRequest = organizationsEntity.list(pageFilter);
      OrganizationsPage organizationsPage = organizationRequest.execute().getBody();
      return PagingHelper.toPage(organizationsPage.getItems(), page, size);
    } catch (Auth0Exception ex) {
      throw VortexException.internalError("Failed to get organizations");
    }
  }

  public Organization findOne(String orgId) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = organizationsEntity.get(orgId).execute().getBody();
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      organization.setEnabledConnections(enabledConnectionsPage.getItems());
      return organization;
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get organization: " + orgId);
    }
  }

  public Paging<Member> listMembers(String orgId, int page, int size) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<MembersPage> request = organizationsEntity.getMembers(orgId, null);
      List<Member> items = request.execute().getBody().getItems();
      return PagingHelper.toPage(items, page, size);
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get members of organization: " + orgId);
    }
  }

  public Invitation createInvitation(
      String orgId, CreateInivitationDto request, String requestedBy) {

    log.info("creating invitation:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);

    if (request.getRoles() != null) {
      for (String roleId : request.getRoles()) {
        Auth0Property.Role role =
            auth0Client.getAuth0Property().getRoles().stream()
                .filter(r -> r.getRoleId().equals(roleId))
                .findFirst()
                .orElse(null);
        if (role == null) {
          throw VortexException.badRequest("Role not found: " + roleId);
        }
        if (role.getOrgIds() != null
            && !role.getOrgIds().isEmpty()
            && !role.getOrgIds().contains(orgId)) {
          throw VortexException.badRequest("Role not found for organization: " + orgId);
        }
      }
    }
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = organizationsEntity.get(orgId).execute().getBody();

      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      if (enabledConnectionsPage.getItems().isEmpty()) {
        throw VortexException.badRequest("No connection found for organization: " + orgId);
      }

      User currentUser =
          this.auth0Client.getMgmtClient().users().get(requestedBy, null).execute().getBody();

      Inviter inviter = new Inviter(currentUser.getName());
      Invitee invitee = new Invitee(request.getEmail());

      Invitation invitation =
          new Invitation(inviter, invitee, auth0Client.getAuth0Property().getApp().getClientId());
      invitation.setConnectionId(enabledConnectionsPage.getItems().get(0).getConnectionId());
      invitation.setSendInvitationEmail(false);
      invitation.setRoles(new Roles(request.getRoles()));

      Request<Invitation> invitationRequest =
          organizationsEntity.createInvitation(orgId, invitation);

      Invitation createdInvitation = invitationRequest.execute().getBody();
      emailService.sendInvitation(createdInvitation);
      return createdInvitation;
    } catch (Auth0Exception e) {
      log.error("create invitations.error", e);
      throw VortexException.internalError("Failed to create invitations of organization: " + orgId);
    }
  }

  public Paging<Invitation> listInvitations(String orgId, int page, int size) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<InvitationsPage> request = organizationsEntity.getInvitations(orgId, null);
      List<Invitation> items = request.execute().getBody().getItems();
      return PagingHelper.toPage(items, page, size);
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get invitations of organization: " + orgId);
    }
  }

  public Invitation getInvitationById(String orgId, String invitationId) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Invitation> request = organizationsEntity.getInvitation(orgId, invitationId, null);
      return request.execute().getBody();
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get invitation: " + invitationId);
    }
  }

  public void deleteInvitation(String orgId, String invitationId, String requestedBy) {
    log.info(
        "deleting invitation:orgId:{}, invitationId:{},requestedBy:{}",
        orgId,
        invitationId,
        requestedBy);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Void> request = organizationsEntity.deleteInvitation(orgId, invitationId);
      request.execute().getBody();
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to delete invitation: " + invitationId);
    }
  }

  public Paging<Role> listRoles(String orgId, int page, int size) {
    List<String> roleIds =
        auth0Client.getAuth0Property().getRoles().stream()
            .filter(role -> role.getRoleId() != null && !role.getRoleId().isEmpty())
            .filter(
                roleSet ->
                    roleSet.getOrgIds() == null
                        || roleSet.getOrgIds().isEmpty()
                        || roleSet.getOrgIds().contains(orgId))
            .map(Auth0Property.Role::getRoleId)
            .distinct()
            .toList();

    try {
      RolesEntity rolesEntity = this.auth0Client.getMgmtClient().roles();
      List<Role> items =
          rolesEntity.list(null).execute().getBody().getItems().stream()
              .filter(role -> roleIds.contains(role.getId()))
              .toList();

      return PagingHelper.toPage(items, page, size);
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get roles of organization: " + orgId);
    }
  }

  public Paging<OrganizationConnection> listConnections(String orgId, int page, int size) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<EnabledConnectionsPage> request = organizationsEntity.getConnections(orgId, null);
      List<EnabledConnection> items = request.execute().getBody().getItems();

      List<com.auth0.json.mgmt.connections.Connection> connections =
          this.auth0Client
              .getMgmtClient()
              .connections()
              .listAll(null)
              .execute()
              .getBody()
              .getItems();

      List<OrganizationConnection> organizationConnections =
          items.stream()
              .map(
                  item -> {
                    OrganizationConnection organizationConnection = new OrganizationConnection();
                    organizationConnection.setConnectionId(item.getConnectionId());
                    organizationConnection.setAssignMembershipOnLogin(
                        item.isAssignMembershipOnLogin());
                    organizationConnection.setShowAsButton(item.getShowAsButton());
                    organizationConnection.setConnection(
                        connections.stream()
                            .filter(connection -> connection.getId().equals(item.getConnectionId()))
                            .findFirst()
                            .get());
                    return organizationConnection;
                  })
              .toList();
      return PagingHelper.toPage(organizationConnections, page, size);
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get connections of organization: " + orgId);
    }
  }

  public OrganizationConnection createConnection(
      String orgId, CreateConnectionDto request, String requestedBy) {
    log.info("creating connection:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);
    try {

      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();

      // only one connection is allowed for an organization
      if (!organizationsEntity
          .getConnections(orgId, null)
          .execute()
          .getBody()
          .getItems()
          .isEmpty()) {
        throw VortexException.badRequest("Connection already exists for organization: " + orgId);
      }

      // create connection
      ConnectionsEntity connectionsEntity = this.auth0Client.getMgmtClient().connections();

      Connection connection =
          new Connection(request.getName(), ConnectionStrategryEnum.OIDC.getValue());
      connection.setEnabledClients(List.of(auth0Client.getAuth0Property().getApp().getClientId()));
      connection.setOptions(request.getOdic().toMap());

      Connection createdConnection = connectionsEntity.create(connection).execute().getBody();

      // bind connection
      EnabledConnection enabledConnection = new EnabledConnection();
      enabledConnection.setConnectionId(createdConnection.getId());
      enabledConnection.setShowAsButton(false);
      enabledConnection.setAssignMembershipOnLogin(false);

      EnabledConnection createdEnabledConnection =
          organizationsEntity.addConnection(orgId, enabledConnection).execute().getBody();

      OrganizationConnection organizationConnection = new OrganizationConnection();
      organizationConnection.setConnectionId(createdEnabledConnection.getConnectionId());
      organizationConnection.setAssignMembershipOnLogin(
          createdEnabledConnection.isAssignMembershipOnLogin());
      organizationConnection.setShowAsButton(createdEnabledConnection.getShowAsButton());
      organizationConnection.setConnection(createdConnection);
      return organizationConnection;

    } catch (Auth0Exception e) {
      log.error("create connections.error", e);
      throw VortexException.internalError("Failed to create connections of organization: " + orgId);
    }
  }
}
