package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.filter.InvitationsFilter;
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
import com.consoleconnect.vortex.iam.util.Auth0PageHelper;
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
  private static final Integer TOTAL_PAGE_SIZE = -1;

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
      throw VortexException.badRequest("create organizations.error: " + e.getMessage());
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
      throw VortexException.badRequest("update organizations.error: " + e.getMessage());
    }
  }

  public Organization updateStatus(String orgId, OrgStatusEnum status, String updatedBy) {
    log.info("updating organization: orgId:{},status:{},updatedBy:{}", orgId, status, updatedBy);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization oldOrg = findOrganization(orgId, organizationsEntity);

      // checking
      Map<String, Object> metadata = oldOrg.getMetadata();
      String oldStatusStr = MapUtils.getString(metadata, OrganizationMetadata.META_STATUS);
      OrgStatusEnum oldStatus = OrgStatusEnum.valueOf(oldStatusStr);
      if (status == oldStatus) {
        throw VortexException.badRequest("The status is the same, orgId:" + orgId);
      }

      Organization update = new Organization();
      metadata.put(OrganizationMetadata.META_STATUS, status);
      update.setMetadata(metadata);
      Response<Organization> updateResponse = organizationsEntity.update(orgId, update).execute();

      Organization organization = updateResponse.getBody();
      // process the related connection
      if (updateResponse.getStatusCode() == HttpStatus.SC_OK) {
        organization =
            processOrgRelatedConnection(
                orgId, status, oldStatus, organizationsEntity, organization);
      }

      return organization;
    } catch (Exception e) {
      log.error("update status of organization.error", e);
      throw VortexException.badRequest("Update status of organizations error: " + e.getMessage());
    }
  }

  private Organization processOrgRelatedConnection(
      String orgId,
      OrgStatusEnum status,
      OrgStatusEnum oldStatus,
      OrganizationsEntity organizationsEntity,
      Organization newOrg)
      throws Auth0Exception {
    log.info(
        "processOrgRelatedConnection, orgId:{}, status:{}, oldStatus:{}", orgId, status, oldStatus);

    Organization updateOrgLoginType = new Organization();
    Map<String, Object> metadata = newOrg.getMetadata();

    // active -> inactive: release the bound connection
    if (oldStatus == OrgStatusEnum.ACTIVE && status == OrgStatusEnum.INACTIVE) {
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(orgId, null).execute().getBody();

      if (Objects.isNull(enabledConnectionsPage)
          || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
        log.info("processOrgRelatedConnection.no related connection, orgId:{}", orgId);
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
    if (oldStatus == OrgStatusEnum.INACTIVE && status == OrgStatusEnum.ACTIVE) {
      ConnectionsPage connectionsPage =
          this.auth0Client.getMgmtClient().connections().listAll(null).execute().getBody();
      if (Objects.isNull(connectionsPage) || CollectionUtils.isEmpty(connectionsPage.getItems())) {
        return newOrg;
      }

      Optional<Connection> connectionOptional =
          connectionsPage.getItems().stream()
              .filter(c -> c.getName().startsWith(StringUtils.join(newOrg.getName(), "-")))
              .findFirst();

      if (connectionOptional.isEmpty()) {
        log.info("processOrgRelatedConnection.no matched connection, orgId:{}", orgId);
        return newOrg;
      }

      Connection connection = connectionOptional.get();
      organizationsEntity.addConnection(orgId, new EnabledConnection(connection.getId())).execute();

      // set login type.
      metadata.put(OrganizationMetadata.META_LOGIN_TYPE, connection.getStrategy());
      updateOrgLoginType.setMetadata(metadata);
    }

    return organizationsEntity.update(orgId, updateOrgLoginType).execute().getBody();
  }

  public Paging<Organization> search(int page, int size) {
    log.info("search organizations, page:{}, size:{}", page, size);
    return Auth0PageHelper.listPageByTotal(
        size,
        page,
        (pageFilterParameters -> {
          try {
            PageFilter invitationsFilter = new PageFilter();
            invitationsFilter.withTotals(pageFilterParameters.isIncludeTotals());
            invitationsFilter.withPage(
                pageFilterParameters.getPage(), pageFilterParameters.getSize());
            OrganizationsEntity organizationsEntity =
                this.auth0Client.getMgmtClient().organizations();
            Request<OrganizationsPage> organizationRequest =
                organizationsEntity.list(invitationsFilter);
            return organizationRequest.execute().getBody();
          } catch (Auth0Exception e) {
            throw VortexException.internalError("Failed to get organizations");
          }
        }));
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
    log.info("list members, orgId:{}, size:{}", orgId, size);
    return Auth0PageHelper.listPageByTotal(
        size,
        page,
        (pageFilterParameters -> {
          try {
            PageFilter invitationsFilter = new PageFilter();
            invitationsFilter.withTotals(pageFilterParameters.isIncludeTotals());
            invitationsFilter.withPage(
                pageFilterParameters.getPage(), pageFilterParameters.getSize());
            OrganizationsEntity organizationsEntity =
                this.auth0Client.getMgmtClient().organizations();
            Request<MembersPage> request = organizationsEntity.getMembers(orgId, invitationsFilter);
            return request.execute().getBody();
          } catch (Auth0Exception e) {
            throw VortexException.internalError("Failed to get members of organization: " + orgId);
          }
        }));
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
      String orgId, CreateInvitationDto request, String requestedBy) {
    log.info("creating invitation:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);

    List<String> roleNames = getAvailableRoleNames();
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
      invitation.setSendInvitationEmail(request.isSendEmail());
      invitation.setRoles(
          new Roles(findRolesByName(request.getRoles()).stream().map(Role::getId).toList()));

      Request<Invitation> invitationRequest =
          organizationsEntity.createInvitation(orgId, invitation);

      Response<Invitation> createdInvitationResponse = invitationRequest.execute();
      Invitation createdInvitation = createdInvitationResponse.getBody();
      emailService.sendInvitation(createdInvitation);
      return createdInvitation;
    } catch (Auth0Exception e) {
      log.error("create invitations.error", e);
      throw VortexException.internalError("Failed to create invitations of organization: " + orgId);
    }
  }

  public Paging<Invitation> listInvitations(String orgId, int page, int size) {
    log.info("list invitations, orgId:{}, size:{}", orgId, size);
    return Auth0PageHelper.listPageByLimit(
        size,
        page,
        (pageFilterParameters -> {
          try {
            InvitationsFilter invitationsFilter = new InvitationsFilter();
            invitationsFilter.withTotals(pageFilterParameters.isIncludeTotals());
            invitationsFilter.withPage(
                pageFilterParameters.getPage(), pageFilterParameters.getSize());
            OrganizationsEntity organizationsEntity =
                this.auth0Client.getMgmtClient().organizations();

            // This endpoint doesn't contain the value of total field in response.
            Request<InvitationsPage> request =
                organizationsEntity.getInvitations(orgId, invitationsFilter);
            return request.execute().getBody();
          } catch (Auth0Exception e) {
            throw VortexException.internalError(
                "Failed to get invitations of organization: " + orgId);
          }
        }));
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

  private List<String> getAvailableRoleNames() {
    List<String> roleNames = new ArrayList<>();
    roleNames.add(RoleEnum.ORG_ADMIN.name());
    roleNames.add(RoleEnum.ORG_MEMBER.name());
    return roleNames;
  }

  public Paging<Role> listRoles(String orgId, int page, int size) {
    log.info("list roles, orgId:{}, size:{}", orgId, size);
    return PagingHelper.toPage(findRolesByName(getAvailableRoleNames()), page, size);
  }

  public OrganizationConnection getOneConnection(String orgId) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<EnabledConnectionsPage> request = organizationsEntity.getConnections(orgId, null);
      List<EnabledConnection> items = request.execute().getBody().getItems();
      if (CollectionUtils.isEmpty(items)) {
        return null;
      }

      EnabledConnection enabledConnection = items.get(0);
      com.auth0.json.mgmt.connections.Connection connections =
          this.auth0Client
              .getMgmtClient()
              .connections()
              .get(enabledConnection.getConnectionId(), null)
              .execute()
              .getBody();

      OrganizationConnection organizationConnection = new OrganizationConnection();
      organizationConnection.setConnectionId(enabledConnection.getConnectionId());
      organizationConnection.setAssignMembershipOnLogin(
          enabledConnection.isAssignMembershipOnLogin());
      organizationConnection.setShowAsButton(enabledConnection.getShowAsButton());
      organizationConnection.setConnection(connections);
      return organizationConnection;

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
      log.info("Updating connection orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);
      Connection connection =
          auth0Client.getMgmtClient().connections().get(request.getId(), null).execute().getBody();
      if (Objects.isNull(connection)) {
        throw VortexException.badRequest("Can't find a connection, id:" + request.getId());
      }
      strategy = connection.getStrategy();
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Update connection error:" + e.getMessage());
    }

    AbstractConnection abstractConnection = connectionMap.get(strategy);
    return abstractConnection.updateConnection(orgId, request, requestedBy);
  }

  public Void resetPassword(String orgId, String memberId, String requestedBy) {
    log.info("orgId:{}, userId:{}, name:{}", orgId, memberId, requestedBy);
    try {
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Member member = findMemberById(orgId, memberId, organizationsEntity);

      String errorMsg = StringUtils.join("Don't support reset password orgId:", orgId);
      com.auth0.json.mgmt.organizations.Connection connection =
          findAuth0Connection(orgId, organizationsEntity, errorMsg);

      return this.auth0Client
          .getAuthClient()
          .resetPassword(member.getEmail(), connection.getName())
          .execute()
          .getBody();
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Reset error:" + e.getMessage());
    }
  }

  private com.auth0.json.mgmt.organizations.Connection findAuth0Connection(
      String orgId, OrganizationsEntity organizationsEntity, String msg) throws Auth0Exception {
    EnabledConnectionsPage enabledConnectionsPage =
        organizationsEntity.getConnections(orgId, null).execute().getBody();
    if (Objects.isNull(enabledConnectionsPage)
        || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
      throw VortexException.badRequest("No connection orgId:" + orgId);
    }

    com.auth0.json.mgmt.organizations.Connection connection =
        enabledConnectionsPage.getItems().get(0).getConnection();
    if (!connection.getStrategy().equals(ConnectionStrategyEnum.AUTH0.getValue())) {
      throw VortexException.badRequest(msg);
    }
    return connection;
  }

  private Member findMemberById(
      String orgId, String userId, OrganizationsEntity organizationsEntity) throws Auth0Exception {
    MembersPage membersPage = organizationsEntity.getMembers(orgId, null).execute().getBody();
    if (Objects.isNull(membersPage) || CollectionUtils.isEmpty(membersPage.getItems())) {
      throw VortexException.badRequest("This organization doesn't has any members. orgId:" + orgId);
    }

    Optional<Member> memberOptional =
        membersPage.getItems().stream().filter(m -> m.getUserId().equals(userId)).findFirst();
    if (memberOptional.isEmpty()) {
      throw VortexException.badRequest(
          "This user doesn't belong to this organization. userId:" + userId);
    }
    return memberOptional.get();
  }

  public Void revokeInvitation(String orgId, String invitationId, String requestedBy) {
    log.info(
        "revokeInvitation, orgId:{}, invitationId:{}, requestedBy:{}",
        orgId,
        invitationId,
        requestedBy);
    try {
      ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Request<Invitation> request = organizationsEntity.getInvitation(orgId, invitationId, null);
      Invitation invitation = request.execute().getBody();
      if (Objects.isNull(invitation)) {
        throw VortexException.badRequest(
            "This user hasn't been invited. invitationId:" + invitationId);
      }
      return organizationsEntity.deleteInvitation(orgId, invitationId).execute().getBody();
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Failed to revoke invitation:" + e.getMessage());
    }
  }

  public User changeMemberStatus(String orgId, String memberId, boolean block, String requestedBy) {
    log.info(
        "blockUser, orgId:{}, memberId:{},block:{}, requestedBy:{}",
        orgId,
        memberId,
        block,
        requestedBy);
    try {
      return doUpdateMember(orgId, memberId, block, null);
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Block/Unblock a user error:" + e.getMessage());
    }
  }

  public User updateMemberInfo(
      String orgId, String memberId, MemberInfoUpdateDto memberInfoUpdateDto, String requestedBy) {
    log.info(
        "updateMemberName, orgId:{}, memberId:{}, memberInfoUpdateDto:{}, requestedBy:{}",
        orgId,
        memberId,
        memberInfoUpdateDto,
        requestedBy);
    try {
      return doUpdateMember(orgId, memberId, null, memberInfoUpdateDto);
    } catch (Auth0Exception e) {
      throw VortexException.badRequest("Update name error:" + e.getMessage());
    }
  }

  private User doUpdateMember(
      String orgId, String memberId, Boolean block, MemberInfoUpdateDto memberInfoUpdateDto)
      throws Auth0Exception {
    ManagementAPI managementAPI = this.auth0Client.getMgmtClient();
    OrganizationsEntity organizationsEntity = managementAPI.organizations();
    Member member = findMemberById(orgId, memberId, organizationsEntity);

    User user = new User();
    if (Objects.nonNull(block)) {
      user.setBlocked(block);
    }

    if (Objects.nonNull(memberInfoUpdateDto)) { // only for db-connection
      String errorMsg = StringUtils.join("Don't support update name, orgId:", orgId);
      findAuth0Connection(orgId, organizationsEntity, errorMsg);
      user.setGivenName(memberInfoUpdateDto.getGivenName());
      user.setFamilyName(memberInfoUpdateDto.getFamilyName());
      user.setName(
          StringUtils.join(
              memberInfoUpdateDto.getGivenName(), " ", memberInfoUpdateDto.getFamilyName()));
    }
    return managementAPI.users().update(member.getUserId(), user).execute().getBody();
  }
}
