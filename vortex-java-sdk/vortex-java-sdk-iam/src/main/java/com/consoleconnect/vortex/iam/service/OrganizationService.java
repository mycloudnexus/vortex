package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ConnectionsEntity;
import com.auth0.client.mgmt.ManagementAPI;
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
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.model.AttributeProperty;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class OrganizationService {

  private final Auth0Client auth0Client;
  private final EmailService emailService;
  private static final String META_STATUS = "status";
  private static final String META_LOGIN_TYPE = "loginType";

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
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      findOrganization(orgId, organizationsEntity);

      Organization organization = new Organization();
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
        metadata.put(META_STATUS, request.getStatus());
      }

      Request<Organization> organizationRequest = organizationsEntity.update(orgId, organization);
      Organization response = organizationRequest.execute().getBody();

      //
      if (organization.getMetadata().get(META_STATUS).equals(OrgStatusEnum.ACTIVE.name())
          && request.getStatus().equals(OrgStatusEnum.INACTIVE)) {
        EnabledConnectionsPage enabledConnectionsPage =
            organizationsEntity.getConnections(orgId, null).execute().getBody();
        if (Objects.nonNull(enabledConnectionsPage)
            && CollectionUtils.isNotEmpty(enabledConnectionsPage.getItems())) {
          Organization updateMetadata = new Organization();
          updateMetadata.setMetadata(Map.of("loginType", LoginTypeEnum.UNDEFINED.name()));
          organizationsEntity.update(orgId, updateMetadata).execute();

          organizationsEntity
              .deleteConnection(orgId, enabledConnectionsPage.getItems().get(0).getConnectionId())
              .execute();
        }
      }
      return response;
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
      return bindOrganizationConnection(
          orgId, createdConnection, organizationsEntity, LoginTypeEnum.SSO);

    } catch (Auth0Exception e) {
      log.error("create connections.error", e);
      throw VortexException.internalError("Failed to create connections of organization: " + orgId);
    }
  }

  public OrganizationConnection dbConnection(String orgId, String requestedBy) {
    try {
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      log.info("creating db connection:orgId:{}, {},requestedBy:{}", orgId, requestedBy);

      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Organization organization = organizationsEntity.get(orgId).execute().getBody();
      if (organization
          .getMetadata()
          .get(META_LOGIN_TYPE)
          .equals(LoginTypeEnum.USERNAME_PASSWORD.name())) {
        throw VortexException.internalError(
            "Failed to create or change db connections of organization: " + orgId);
      }

      this.cleanConnectionAndMembers(managementAPI, organizationsEntity, organization.getName());

      Connection connection =
          new Connection(
              StringUtils.join(organization.getName(), "-", ConnectionStrategryEnum.DB.getValue()),
              ConnectionStrategryEnum.DB.getValue());
      Map<String, Object> options = new HashMap<>();
      options.put("disable_signup", false);

      AttributeProperty attribute = new AttributeProperty();
      options.put("email", attribute);
      connection.setOptions(options);
      connection.setEnabledClients(List.of(auth0Client.getAuth0Property().getApp().getClientId()));

      Connection createdConnection =
          auth0Client.getMgmtClient().connections().create(connection).execute().getBody();
      return bindOrganizationConnection(
          orgId, createdConnection, organizationsEntity, LoginTypeEnum.USERNAME_PASSWORD);
    } catch (Auth0Exception e) {
      log.error("create db connections.error", e);
      throw VortexException.internalError(
          "Failed to create db connections of organization: " + orgId);
    }
  }

  public OrganizationConnection createOrUpdateSAML(
      String orgId, SamlConnection samlConnection, String shortName) {
    try {
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      Organization organization = managementAPI.organizations().get(orgId).execute().getBody();
      Map<String, Object> metaData =
          JsonToolkit.createObjectMapper().convertValue(samlConnection.getOptions(), Map.class);

      if (StringUtils.isNoneBlank(samlConnection.getId())) { // edit
        if (!organization.getMetadata().get(META_LOGIN_TYPE).equals(LoginTypeEnum.SSO.name())) {
          throw VortexException.internalError(
              "Failed to change saml connections of organization: " + orgId);
        }

        Request<Connection> response =
            managementAPI.connections().get(samlConnection.getId(), null);
        Connection connection = response.execute().getBody();

        Connection update = new Connection();
        Map<String, Object> originalMeta = connection.getOptions();
        originalMeta.putAll(metaData);
        update.setOptions(originalMeta);

        Connection updatedConnection =
            managementAPI.connections().update(connection.getId(), update).execute().getBody();

        OrganizationConnection organizationConnection = new OrganizationConnection();
        organizationConnection.setConnectionId(connection.getId());
        organizationConnection.setAssignMembershipOnLogin(true);
        organizationConnection.setShowAsButton(false);
        organizationConnection.setConnection(updatedConnection);
        return organizationConnection;

      } else { // new
        OrganizationsEntity organizationsEntity = managementAPI.organizations();
        if (organization.getMetadata().get(META_LOGIN_TYPE).equals(LoginTypeEnum.SSO.name())) {
          throw VortexException.internalError(
              "Failed to create saml connections of organization: " + orgId);
        }

        // 1. check and clean existed members & connection.
        this.cleanConnectionAndMembers(managementAPI, organizationsEntity, shortName);

        // 2. create new sso connection
        Connection connection =
            new Connection(
                StringUtils.join(
                    organization.getName(), "-", ConnectionStrategryEnum.DB.getValue()),
                samlConnection.getStrategy());
        connection.setOptions(metaData);
        connection.setEnabledClients(
            List.of(auth0Client.getAuth0Property().getApp().getClientId()));

        Connection createdConnection =
            managementAPI.connections().create(connection).execute().getBody();

        // 3. bind connection
        return bindOrganizationConnection(
            orgId, createdConnection, organizationsEntity, LoginTypeEnum.SSO);
      }
    } catch (Exception e) {
      log.error("[module-auth]create.connection error", e);
      throw VortexException.badRequest("Create connection error" + e.getMessage());
    }
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
    enabledConnection.setShowAsButton(false);
    enabledConnection.setAssignMembershipOnLogin(false);

    EnabledConnection createdEnabledConnection =
        organizationsEntity.addConnection(orgId, enabledConnection).execute().getBody();

    Organization updateMetadata = new Organization();
    updateMetadata.setMetadata(Map.of("loginType", loginTypeEnum.name()));
    organizationsEntity.update(orgId, updateMetadata).execute();

    OrganizationConnection organizationConnection = new OrganizationConnection();
    organizationConnection.setConnectionId(createdEnabledConnection.getConnectionId());
    organizationConnection.setAssignMembershipOnLogin(
        createdEnabledConnection.isAssignMembershipOnLogin());
    organizationConnection.setShowAsButton(createdEnabledConnection.getShowAsButton());
    organizationConnection.setConnection(createdConnection);
    return organizationConnection;
  }

  public void cleanConnectionAndMembers(
      ManagementAPI managementAPI, OrganizationsEntity organizationsEntity, String orgId) {
    try {
      Organization organization = organizationsEntity.get(orgId).execute().getBody();

      // delete members
      MembersPage membersPage =
          organizationsEntity.getMembers(organization.getId(), null).execute().getBody();
      if (Objects.nonNull(membersPage)
          && !org.springframework.util.CollectionUtils.isEmpty(membersPage.getItems())) {
        List<Member> members = membersPage.getItems();
        for (Member m : members) {
          managementAPI.users().delete(m.getUserId()).execute();
        }
      }

      // delete invitations
      InvitationsPage invitationsPage =
          organizationsEntity.getInvitations(organization.getId(), null).execute().getBody();
      if (Objects.nonNull(invitationsPage)
          && !org.springframework.util.CollectionUtils.isEmpty(invitationsPage.getItems())) {
        List<Invitation> invitations = invitationsPage.getItems();
        for (Invitation invitation : invitations) {
          organizationsEntity.deleteInvitation(organization.getId(), invitation.getId()).execute();
        }
      }

      // delete connection
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      if (Objects.nonNull(enabledConnectionsPage)
          && !org.springframework.util.CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
        List<EnabledConnection> enabledConnections = enabledConnectionsPage.getItems();
        for (EnabledConnection enabledConnection : enabledConnections) {
          managementAPI
              .connections()
              .delete(enabledConnection.getConnectionId())
              .execute()
              .getStatusCode();
        }
      }
    } catch (Exception e) {
      log.error("cleanMembers.error", e);
    }
  }
}
