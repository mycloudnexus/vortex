package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.connections.ConnectionsPage;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.core.toolkit.PatternHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.service.connection.AbstractConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class OrganizationService {

  private final Auth0Client auth0Client;
  private final EmailService emailService;
  private final Map<String, AbstractConnection> connectionMap;

  public Organization create(CreateOrganizationDto request, String createdBy) {
    log.info("creating organization: {},requestedBy:{}", request, createdBy);
    try {
      check(request);
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = new Organization(request.getName());
      organization.setDisplayName(request.getDisplayName());

      // set default metadata values
      if (request.getMetadata() == null) {
        request.setMetadata(new OrganizationMetadata());
      }

      Map<String, Object> metadata =
          JsonToolkit.fromJson(JsonToolkit.toJson(request.getMetadata()), new TypeReference<>() {});
      organization.setMetadata(metadata);

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
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      findOrganization(orgId, organizationsEntity);

      Organization organization = new Organization();
      if (StringUtils.isNotBlank(request.getDisplayName())) {
        if (request.getDisplayName().length() > 255) {
          throw VortexException.badRequest("Display name cannot exceed 255 characters.");
        }
        organization.setDisplayName(request.getDisplayName());
      }

      Request<Organization> organizationRequest = organizationsEntity.update(orgId, organization);
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("update organizations.error", e);
      throw VortexException.badRequest("update organizations.error" + e.getMessage());
    }
  }

  public Organization updateStatus(String orgId, OrgStatusEnum status, String updatedBy) {
    log.info("updating organization: orgId:{},status:{},updatedBy:{}", orgId, status, updatedBy);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization oldOrg = findOrganization(orgId, organizationsEntity);

      // checking
      Map<String, Object> metadata =
          oldOrg.getMetadata() == null ? new HashMap<>() : oldOrg.getMetadata();
      String oldStatus = MapUtils.getString(metadata, OrganizationMetadata.META_STATUS);
      if (status.name().equals(oldStatus)) {
        throw VortexException.badRequest("The status is the same, orgId:" + orgId);
      }

      Organization update = new Organization();
      metadata.put(OrganizationMetadata.META_STATUS, status);
      update.setMetadata(metadata);
      Response<Organization> updateResponse = organizationsEntity.update(orgId, update).execute();

      Organization organization = updateResponse.getBody();
      // process the related connection
      if (updateResponse.getStatusCode() == HttpStatus.SC_OK && StringUtils.isNotBlank(oldStatus)) {
        organization =
            processOrgRelatedConnection(
                orgId, status, oldStatus, organizationsEntity, organization);
      }

      return organization;
    } catch (Exception e) {
      log.error("update status of organization.error", e);
      throw VortexException.badRequest("Update status of organizations.error" + e.getMessage());
    }
  }

  private Organization processOrgRelatedConnection(
      String orgId,
      OrgStatusEnum status,
      String oldStatus,
      OrganizationsEntity organizationsEntity,
      Organization newOrg)
      throws Auth0Exception {
    Organization updateOrgLoginType = new Organization();
    Map<String, Object> metadata =
        newOrg.getMetadata() == null ? new HashMap<>() : newOrg.getMetadata();

    // active -> inactive: release the bound connection
    if (oldStatus.equals(OrgStatusEnum.ACTIVE.name()) && status == OrgStatusEnum.INACTIVE) {
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(orgId, null).execute().getBody();

      if (Objects.isNull(enabledConnectionsPage)
          || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
        return newOrg;
      }

      organizationsEntity
          .deleteConnection(orgId, enabledConnectionsPage.getItems().get(0).getConnectionId())
          .execute();

      // change login type to undefined
      metadata.put(
          OrganizationMetadata.META_LOGIN_TYPE, ConnectionStrategyEnum.UNDEFINED.getValue());
      updateOrgLoginType.setMetadata(metadata);
    }

    // inactive -> active: check and bind the existed connection which has been released.
    if (oldStatus.equals(OrgStatusEnum.INACTIVE.name()) && status == OrgStatusEnum.ACTIVE) {
      ConnectionsPage connectionsPage =
          this.auth0Client.getMgmtClient().connections().listAll(null).execute().getBody();
      if (Objects.isNull(connectionsPage) || CollectionUtils.isEmpty(connectionsPage.getItems())) {
        return newOrg;
      }

      Optional<Connection> existedConnection =
          connectionsPage.getItems().stream()
              .filter(c -> c.getName().startsWith(StringUtils.join(newOrg.getName(), "-")))
              .findFirst();

      if (existedConnection.isPresent()) {
        Connection connection = existedConnection.get();
        organizationsEntity
            .addConnection(orgId, new EnabledConnection(connection.getId()))
            .execute();

        // set login type.

        metadata.put(OrganizationMetadata.META_LOGIN_TYPE, connection.getStrategy());
        updateOrgLoginType.setMetadata(metadata);
      }
    }

    return organizationsEntity.update(orgId, updateOrgLoginType).execute().getBody();
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
      Organization organization = findOrganization(orgId, organizationsEntity);
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      organization.setEnabledConnections(enabledConnectionsPage.getItems());
      return organization;
    } catch (Auth0Exception e) {
      throw VortexException.internalError("Failed to get organization: " + orgId);
    }
  }

  private Organization findOrganization(String orgId, OrganizationsEntity organizationsEntity) {
    try {
      return organizationsEntity.get(orgId).execute().getBody();
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

  private List<Role> findRolesByName(List<String> roleNames) {
    try {
      RolesEntity rolesEntity = this.auth0Client.getMgmtClient().roles();
      return rolesEntity.list(null).execute().getBody().getItems().stream()
          .filter(role -> roleNames.contains(role.getName()))
          .toList();
    } catch (Auth0Exception e) {
      log.error("convertRoleNameId.error", e);
      throw VortexException.internalError("Failed to convert role name to id");
    }
  }

  public Invitation createInvitation(
      String orgId, CreateInivitationDto request, String requestedBy) {

    log.info("creating invitation:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);

    if (request.getRoles() == null || request.getRoles().isEmpty()) {
      throw VortexException.badRequest("Roles cannot be empty.");
    }
    if (request.getEmail() == null || request.getEmail().isBlank()) {
      throw VortexException.badRequest("Email cannot be empty.");
    }

    List<String> roleNames = getAvailableRoleNames(orgId);

    if (request.getRoles().stream().anyMatch(role -> !roleNames.contains(role))) {
      throw VortexException.badRequest("Role not found for organization: " + orgId);
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
      invitation.setRoles(
          new Roles(findRolesByName(request.getRoles()).stream().map(Role::getId).toList()));

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

  private List<String> getAvailableRoleNames(String orgId) {
    List<String> roleNames = new ArrayList<>();
    roleNames.add(RoleEnum.ORG_ADMIN.name());
    roleNames.add(RoleEnum.ORG_MEMBER.name());
    if (auth0Client.getAuth0Property().getMgmtOrgId().equalsIgnoreCase(orgId)) {
      // the user in mgmt organization can see all roles
      roleNames.add(RoleEnum.PLATFORM_ADMIN.name());
      roleNames.add(RoleEnum.PLATFORM_MEMBER.name());
    }
    return roleNames;
  }

  public Paging<Role> listRoles(String orgId, int page, int size) {
    return PagingHelper.toPage(findRolesByName(getAvailableRoleNames(orgId)), page, size);
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
    AbstractConnection connection = connectionMap.get(request.getStrategy().getValue());
    if (Objects.isNull(connection)) {
      throw VortexException.badRequest("Invalid connection strategy");
    }
    return connection.createConnection(orgId, request);
  }

  public OrganizationConnection updateConnection(
      String orgId, UpdateConnectionDto request, String requestedBy) {
    String strategy;
    try {
      log.info("Updating connection:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);
      Connection connection =
          auth0Client.getMgmtClient().connections().get(request.getId(), null).execute().getBody();
      strategy = connection.getStrategy();
      if (Objects.isNull(connection)) {
        throw VortexException.badRequest("Can't find a connection, id:" + request.getId());
      }
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Update connection error:" + e.getMessage());
    }

    AbstractConnection abstractConnection = connectionMap.get(strategy);
    return abstractConnection.updateConnection(orgId, request, requestedBy);
  }
}
