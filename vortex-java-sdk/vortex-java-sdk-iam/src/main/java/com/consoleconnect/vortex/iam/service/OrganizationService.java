package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.filter.InvitationsFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.mapper.MemberMapper;
import com.consoleconnect.vortex.iam.mapper.OrganizationMapper;
import com.consoleconnect.vortex.iam.service.connection.AbstractConnectionProvider;
import com.consoleconnect.vortex.iam.toolkit.Auth0PageHelper;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  private final List<AbstractConnectionProvider> connectionProviders;
  private final AuthTokenService authTokenService;

  private VortexException badRequest(Auth0Exception e, String msg) {
    String errorMsg = StringUtils.join(msg, e.getMessage());
    log.error("{}", errorMsg);
    return VortexException.badRequest(errorMsg);
  }

  public OrganizationInfo create(CreateOrganizationDto request, String createdBy) {
    log.info("creating organization: {},requestedBy:{}", request, createdBy);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = new Organization(request.getName());
      organization.setDisplayName(request.getDisplayName());
      organization.setMetadata(OrganizationMetadata.toMap(request.getMetadata()));
      return OrganizationMapper.INSTANCE.toOrganizationInfo(
          organizationsEntity.create(organization).execute().getBody());
    } catch (Auth0Exception e) {
      throw badRequest(e, "create organization,error:");
    }
  }

  public OrganizationInfo update(String orgId, UpdateOrganizationDto request, String createdBy) {
    log.info("updating organization: {},{},requestedBy:{}", orgId, request, createdBy);
    Organization organization = findOrganizationAndThrow(orgId);
    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());

    Organization updateRequest = new Organization();
    if (request.getDisplayName() != null) {
      updateRequest.setDisplayName(request.getDisplayName());
    }
    if (request.getStatus() != null) {
      if (metadata.getStatus() == request.getStatus()) {
        throw VortexException.badRequest("The status is the same, orgId:" + orgId);
      }
      metadata.setStatus(request.getStatus());
      updateRequest.setMetadata(OrganizationMetadata.toMap(metadata));
    }

    organization = doUpdateOrganization(organization.getId(), updateRequest);

    try {
      if (request.getStatus() == OrgStatusEnum.INACTIVE) {
        doDeleteEnabledConnection(organization.getId());
      } else if (request.getStatus() == OrgStatusEnum.ACTIVE) {
        doRestoreEnabledConnection(organization.getId(), metadata);
      }
    } catch (Auth0Exception e) {
      log.error("update organization status error, orgId:{}", orgId);
    }

    return OrganizationMapper.INSTANCE.toOrganizationInfo(organization);
  }

  private Organization doUpdateOrganization(String orgId, Organization updateRequest) {
    log.info("doUpdateOrganization, orgId:{},payload:{}", orgId, updateRequest);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      return organizationsEntity.update(orgId, updateRequest).execute().getBody();
    } catch (Auth0Exception e) {
      throw badRequest(e, String.format("update organization(%s),error:", orgId));
    }
  }

  private void doDeleteEnabledConnection(String orgId) throws Auth0Exception {

    log.info("doDeleteEnabledConnection, orgId:{}", orgId);
    OrganizationsEntity organizationsEntity = auth0Client.getMgmtClient().organizations();
    EnabledConnectionsPage enabledConnectionsPage =
        organizationsEntity.getConnections(orgId, null).execute().getBody();

    if (Objects.isNull(enabledConnectionsPage)
        || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
      log.info("No enabled connection found for organization:{}", orgId);
      return;
    }

    for (EnabledConnection enabledConnection : enabledConnectionsPage.getItems()) {
      log.info("delete enabled connection, orgId:{}, connectionId:{}", orgId, enabledConnection);
      organizationsEntity.deleteConnection(orgId, enabledConnection.getConnectionId()).execute();
    }
    log.info("doDeleteEnabledConnection, orgId:{} done", orgId);
  }

  private void doRestoreEnabledConnection(String orgId, OrganizationMetadata metadata)
      throws Auth0Exception {

    log.info("doRestoreEnabledConnection, orgId:{},metadata:{}", orgId, metadata);
    if (metadata.getConnectionId() == null) {
      log.info("No connection found for organization:{}", orgId);
      return;
    }

    Optional<Connection> connectionOptional = getOrganizationConnection(metadata.getConnectionId());
    if (connectionOptional.isEmpty()) {
      log.warn(
          "No connection found for organization:{},connectionId:{}",
          orgId,
          metadata.getConnectionId());
      return;
    }

    Optional<AbstractConnectionProvider> connectionProviderOptional =
        getConnectionProviderByStrategy(metadata.getStrategy());
    if (connectionProviderOptional.isEmpty()) {
      log.warn("No connection provider found for strategy:{}", metadata.getStrategy());
      return;
    }

    log.info(
        "restore enabled connection, orgId:{}, connectionId:{}", orgId, metadata.getConnectionId());

    EnabledConnection enabledConnection = new EnabledConnection();
    enabledConnection.setConnectionId(connectionOptional.get().getId());
    enabledConnection.setShowAsButton(true);
    enabledConnection.setAssignMembershipOnLogin(
        connectionProviderOptional.get().assignMembershipOnLogin());
    this.auth0Client
        .getMgmtClient()
        .organizations()
        .addConnection(orgId, enabledConnection)
        .execute();
    log.info("doRestoreEnabledConnection, orgId:{} done", orgId);
  }

  public Paging<OrganizationInfo> search(int page, int size) {
    log.info("search organizations, page:{}, size:{}", page, size);
    Paging<Organization> organizationPaging =
        Auth0PageHelper.loadData(
            page,
            size,
            (pageFilterParameters -> {
              try {
                return this.auth0Client
                    .getMgmtClient()
                    .organizations()
                    .list(pageFilterParameters.toPageFilter())
                    .execute()
                    .getBody();
              } catch (Auth0Exception e) {
                throw badRequest(e, "search organizations,error:");
              }
            }));

    return PagingHelper.toPageNoSubList(
        organizationPaging.getData().stream()
            .map(OrganizationMapper.INSTANCE::toOrganizationInfo)
            .toList(),
        organizationPaging.getPage(),
        organizationPaging.getSize(),
        organizationPaging.getTotal());
  }

  public OrganizationInfo findOne(String orgId) {
    log.info("find organization info, orgId:{}", orgId);

    Organization organization =
        findOrganization(orgId)
            .orElseThrow(
                () -> VortexException.badRequest("Organization not found, orgId:" + orgId));

    OrganizationInfo organizationInfo =
        OrganizationMapper.INSTANCE.toOrganizationInfo(organization);
    getOrganizationConnection(organizationInfo.getMetadata().getConnectionId())
        .ifPresent(organizationInfo::setConnection);

    return organizationInfo;
  }

  public Optional<Organization> findOrganization(String orgId) {
    log.info("find organization, orgId:{}", orgId);
    try {
      return Optional.of(
          this.auth0Client.getMgmtClient().organizations().get(orgId).execute().getBody());
    } catch (Auth0Exception e) {
      log.warn("find organization, orgId:{}", orgId);
      return Optional.empty();
    }
  }

  public Organization findOrganizationAndThrow(String orgId) {
    return findOrganizationAndThrow(orgId, null, null);
  }

  public Organization findOrganizationAndThrow(
      String orgId, OrgStatusEnum orgStatusEnum, Boolean connectionEnabled) {
    return findOrganizationAndThrow(orgId, orgStatusEnum, connectionEnabled, null);
  }

  public Organization findOrganizationAndThrow(
      String orgId,
      OrgStatusEnum orgStatusEnum,
      Boolean connectionEnabled,
      ConnectionStrategyEnum strategy) {
    Organization organization =
        findOrganization(orgId)
            .orElseThrow(
                () -> VortexException.badRequest("Organization not found, orgId:" + orgId));

    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());
    if (orgStatusEnum != null && metadata.getStatus() != orgStatusEnum) {
      throw VortexException.badRequest("Organization status is not " + orgStatusEnum);
    }

    if (connectionEnabled != null) {
      if (connectionEnabled) {
        if (metadata.getConnectionId() == null) {
          throw VortexException.badRequest("No connection found for organization: " + orgId);
        }
      } else {
        if (metadata.getConnectionId() != null) {
          throw VortexException.badRequest("Connection already exists for organization: " + orgId);
        }
      }
    }

    if (strategy != null && metadata.getStrategy() != strategy) {
      throw VortexException.badRequest("Organization connection strategy is not " + strategy);
    }
    return organization;
  }

  public Paging<MemberInfo> listMembers(String orgId, int page, int size) {
    log.info("list members, orgId:{}, size:{}", orgId, size);

    Optional<Connection> connectionOptional = this.getOrganizationConnection(orgId);
    if (connectionOptional.isEmpty()) {
      return PagingHelper.toPage(List.of(), page, size);
    }
    Paging<Member> memberPaging =
        Auth0PageHelper.loadData(
            page,
            size,
            (pageFilterParameters -> {
              try {
                return this.auth0Client
                    .getMgmtClient()
                    .organizations()
                    .getMembers(orgId, pageFilterParameters.toPageFilter())
                    .execute()
                    .getBody();
              } catch (Auth0Exception e) {
                throw VortexException.internalError(
                    "Failed to get members of organization: " + orgId);
              }
            }));

    if (Objects.isNull(memberPaging) || CollectionUtils.isEmpty(memberPaging.getData())) {
      return PagingHelper.toPageNoSubList(Collections.emptyList(), page, size, null);
    }

    Paging<User> userPaging =
        Auth0PageHelper.loadData(
            0,
            -1,
            (pageFilterParameters -> {
              try {
                return this.auth0Client
                    .getMgmtClient()
                    .users()
                    .list(
                        new UserFilter()
                            .withSearchEngine("v2")
                            .withQuery(
                                String.format(
                                    "identities.connection:\"%s\"",
                                    connectionOptional.get().getName()))
                            .withPage(
                                pageFilterParameters.getPage(), pageFilterParameters.getSize())
                            .withTotals(true))
                    .execute()
                    .getBody();
              } catch (Auth0Exception e) {
                throw VortexException.internalError(
                    "Failed to get users of organization: " + orgId);
              }
            }));

    Map<String, Member> memberMap =
        memberPaging.getData().stream()
            .collect(Collectors.toMap(Member::getUserId, Function.identity()));

    List<MemberInfo> memberInfos =
        userPaging.getData().stream()
            .filter(user -> memberMap.containsKey(user.getId()))
            .map(MemberMapper.INSTANCE::toMemberInfo)
            .toList();

    return PagingHelper.toPageNoSubList(
        memberInfos, memberPaging.getPage(), memberPaging.getSize(), memberPaging.getTotal());
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
      String orgId, CreateInvitationDto request, AuthToken authToken) {
    log.info(
        "creating invitation:orgId:{}, {},requestedBy:{}", orgId, request, authToken.getUserId());

    List<String> roleNames = getAvailableRoleNames();
    if (request.getRoles().stream().anyMatch(role -> !roleNames.contains(role))) {
      throw VortexException.badRequest("Role not found for organization: " + orgId);
    }

    Organization organization = findOrganizationAndThrow(orgId, OrgStatusEnum.ACTIVE, true);
    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();

      Inviter inviter = new Inviter(authToken.getName());
      Invitee invitee = new Invitee(request.getEmail());

      Invitation invitation =
          new Invitation(inviter, invitee, auth0Client.getAuth0Property().getApp().getClientId());
      invitation.setConnectionId(metadata.getConnectionId());
      invitation.setSendInvitationEmail(request.isSendEmail());
      invitation.setRoles(
          new Roles(findRolesByName(request.getRoles()).stream().map(Role::getId).toList()));

      Request<Invitation> invitationRequest =
          organizationsEntity.createInvitation(orgId, invitation);

      Response<Invitation> createdInvitationResponse = invitationRequest.execute();
      Invitation createdInvitation = createdInvitationResponse.getBody();
      if (!request.isSendEmail()) { // not send via auth0, then need to handle internal
        emailService.sendInvitation(createdInvitation, false);
      }

      return createdInvitation;
    } catch (Auth0Exception e) {
      throw badRequest(e, String.format("create invitation for organization(%s),error:", orgId));
    }
  }

  public Paging<Invitation> listInvitations(String orgId, int page, int size) {
    log.info("list invitations, orgId:{}, size:{}", orgId, size);
    return Auth0PageHelper.loadData(
        page,
        size,
        (pageFilterParameters -> {
          try {
            return this.auth0Client
                .getMgmtClient()
                .organizations()
                .getInvitations(
                    orgId,
                    new InvitationsFilter()
                        .withPage(pageFilterParameters.getPage(), pageFilterParameters.getSize())
                        .withTotals(pageFilterParameters.isIncludeTotals()))
                .execute()
                .getBody();

          } catch (Auth0Exception e) {
            throw badRequest(e, "list invitations error:");
          }
        }));
  }

  public Invitation getInvitationById(String orgId, String invitationId) {
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Request<Invitation> request = organizationsEntity.getInvitation(orgId, invitationId, null);
      return request.execute().getBody();
    } catch (Auth0Exception e) {
      throw badRequest(
          e, String.format("get invitation(%s) of organization(%s),error:", invitationId, orgId));
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
    this.findOrganizationAndThrow(orgId);
    return PagingHelper.toPage(findRolesByName(getAvailableRoleNames()), page, size);
  }

  public Optional<Connection> getOrganizationConnection(String connectionId) {
    if (StringUtils.isBlank(connectionId)) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          this.auth0Client
              .getMgmtClient()
              .connections()
              .get(connectionId, null)
              .execute()
              .getBody());
    } catch (Auth0Exception e) {
      log.warn("get connection error, connectionId:{}", connectionId);
      return Optional.empty();
    }
  }

  private AbstractConnectionProvider getConnectionProviderByStrategyAndThrow(
      ConnectionStrategyEnum strategy) {
    return getConnectionProviderByStrategy(strategy)
        .orElseThrow(() -> VortexException.badRequest("Invalid connection strategy"));
  }

  private Optional<AbstractConnectionProvider> getConnectionProviderByStrategy(
      ConnectionStrategyEnum strategy) {
    return connectionProviders.stream()
        .filter(p -> p.getConnectionStrategy() == strategy)
        .findFirst();
  }

  public void doDeleteConnection(Organization organization, String connectionId) {
    String orgId = organization.getId();
    log.info("doDeleteConnection, orgId:{}, connectionId:{}", orgId, connectionId);
    try {
      OrganizationsEntity organizationsEntity = auth0Client.getMgmtClient().organizations();
      // delete members
      log.info("delete members from org:{}", organization.getId());
      MembersPage membersPage = organizationsEntity.getMembers(orgId, null).execute().getBody();
      if (membersPage.getItems() != null && !membersPage.getItems().isEmpty()) {
        List<Member> members = membersPage.getItems();
        organizationsEntity
            .deleteMembers(orgId, new Members(members.stream().map(Member::getUserId).toList()))
            .execute();
      }

      // delete invitations
      log.info("delete invitations from org:{}", organization.getId());
      InvitationsPage invitationsPage =
          organizationsEntity.getInvitations(orgId, null).execute().getBody();
      if (invitationsPage.getItems() != null) {
        for (Invitation invitation : invitationsPage.getItems()) {
          organizationsEntity.deleteInvitation(orgId, invitation.getId()).execute();
        }
      }

      // delete enabled connection from org
      log.info("delete enabled connection from org:{}", organization.getId());
      organizationsEntity.deleteConnection(orgId, connectionId).execute();

      // delete connection
      log.info("delete connection:{}", connectionId);
      this.auth0Client.getMgmtClient().connections().delete(connectionId).execute();
    } catch (Auth0Exception e) {
      throw badRequest(e, "delete connection error:");
    }
  }

  public Connection doCreateConnection(
      Organization organization,
      AbstractConnectionProvider connectionProvider,
      CreateConnectionDto request) {

    log.info(
        "doCreateConnection, orgId:{}, request:{}", organization.getId(), request.getStrategy());
    try {
      log.info("create connection for org:{}", organization.getId());
      Connection createdConnection =
          this.auth0Client
              .getMgmtClient()
              .connections()
              .create(connectionProvider.buildConnectionPayload(organization, request))
              .execute()
              .getBody();

      // enable connection for organization
      log.info("enable connection for org:{}", organization.getId());
      EnabledConnection enabledConnection = new EnabledConnection();
      enabledConnection.setConnectionId(createdConnection.getId());
      enabledConnection.setShowAsButton(true);
      enabledConnection.setAssignMembershipOnLogin(connectionProvider.assignMembershipOnLogin());

      this.auth0Client
          .getMgmtClient()
          .organizations()
          .addConnection(organization.getId(), enabledConnection)
          .execute();
      return createdConnection;
    } catch (Auth0Exception e) {
      throw badRequest(e, "create connection error:");
    }
  }

  public Connection createConnection(
      String orgId, CreateConnectionDto request, String requestedBy) {
    log.info("creating connection:orgId:{}, {},requestedBy:{}", orgId, request, requestedBy);
    AbstractConnectionProvider connectionProvider =
        getConnectionProviderByStrategyAndThrow(request.getStrategy());
    Organization organization =
        this.findOrganization(orgId)
            .orElseThrow(() -> VortexException.badRequest("Organization not found"));
    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());
    if (metadata.getStatus() == OrgStatusEnum.INACTIVE) {
      throw VortexException.badRequest("Organization is inactive");
    }

    // delete existing connection
    doDeleteConnection(organization, metadata.getConnectionId());

    // create a new connection
    Connection createdConnection = doCreateConnection(organization, connectionProvider, request);

    // update organization's metadata
    metadata.setConnectionId(createdConnection.getId());
    metadata.setStrategy(request.getStrategy());

    Organization updateOrganization = new Organization();
    updateOrganization.setMetadata(OrganizationMetadata.toMap(metadata));
    doUpdateOrganization(organization.getId(), updateOrganization);

    return createdConnection;
  }

  public Connection updateConnection(
      String orgId, UpdateConnectionDto request, String requestedBy) {

    log.info("updateConnection, orgId:{}, request:{}, requestedBy:{}", orgId, request, requestedBy);
    Organization organization = this.findOrganizationAndThrow(orgId, OrgStatusEnum.ACTIVE, true);

    OrganizationInfo organizationInfo =
        OrganizationMapper.INSTANCE.toOrganizationInfo(organization);
    String connectionId = organizationInfo.getMetadata().getConnectionId();

    ConnectionStrategyEnum strategy = organizationInfo.getMetadata().getStrategy();
    AbstractConnectionProvider abstractConnectionProvider =
        getConnectionProviderByStrategyAndThrow(strategy);
    Connection connection =
        this.getOrganizationConnection(connectionId)
            .orElseThrow(
                () -> VortexException.badRequest("No connection found for organization: " + orgId));
    Connection updateConnectionRequest =
        abstractConnectionProvider.buildConnectionPayload(organization, connection, request);

    try {
      return this.auth0Client
          .getMgmtClient()
          .connections()
          .update(connectionId, updateConnectionRequest)
          .execute()
          .getBody();
    } catch (Auth0Exception e) {
      throw badRequest(e, "update connection error:");
    }
  }

  public Void resetPassword(String orgId, String memberId, String requestedBy) {
    log.info("orgId:{}, userId:{}, name:{}", orgId, memberId, requestedBy);

    Organization organization =
        findOrganizationAndThrow(orgId, OrgStatusEnum.ACTIVE, true, ConnectionStrategyEnum.AUTH0);
    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());

    Member member =
        findMemberById(orgId, memberId)
            .orElseThrow(() -> VortexException.badRequest("Member not found"));

    Connection connection =
        getOrganizationConnection(metadata.getConnectionId())
            .orElseThrow(() -> VortexException.badRequest("Connection not found"));
    try {
      return this.auth0Client
          .getAuthClient()
          .resetPassword(member.getEmail(), connection.getName())
          .execute()
          .getBody();
    } catch (Auth0Exception e) {
      throw badRequest(
          e, String.format("reset password for orgId:%s, userId:%s, error:", orgId, memberId));
    }
  }

  public User findUserById(String orgId, String memberId) {
    log.info("find user by id, orgId:{}, memberId:{}", orgId, memberId);
    try {
      return this.auth0Client.getMgmtClient().users().get(memberId, null).execute().getBody();
    } catch (Auth0Exception e) {
      log.warn("find user by id error, orgId:{}, memberId:{}", orgId, memberId);
      throw badRequest(
          e, String.format("find user by id error, orgId:%s, memberId:%s", orgId, memberId));
    }
  }

  private Optional<Member> findMemberById(String orgId, String memberId) {
    try {
      MembersPage membersPage =
          this.auth0Client
              .getMgmtClient()
              .organizations()
              .getMembers(orgId, null)
              .execute()
              .getBody();
      return membersPage.getItems().stream()
          .filter(m -> m.getUserId().equals(memberId))
          .findFirst();
    } catch (Auth0Exception e) {
      log.warn("find member by id error, orgId:{}, memberId:{}", orgId, memberId);
      return Optional.empty();
    }
  }

  public Void revokeInvitation(String orgId, String invitationId, String requestedBy) {
    log.info(
        "revokeInvitation, orgId:{}, invitationId:{}, requestedBy:{}",
        orgId,
        invitationId,
        requestedBy);
    try {
      return this.auth0Client
          .getMgmtClient()
          .organizations()
          .deleteInvitation(orgId, invitationId)
          .execute()
          .getBody();
    } catch (Auth0Exception e) {
      throw badRequest(
          e,
          String.format(
              "revoke invitation for orgId:%s, invitationId:%s, error:", orgId, invitationId));
    }
  }

  public User updateMember(
      String orgId, String memberId, UpdateMemberDto updateMemberDto, String requestedBy) {
    log.info(
        "updateMemberName, orgId:{}, memberId:{}, memberInfoUpdateDto:{}, requestedBy:{}",
        orgId,
        memberId,
        updateMemberDto,
        requestedBy);
    Organization organization = findOrganizationAndThrow(orgId, OrgStatusEnum.ACTIVE, true);
    OrganizationMetadata metadata = OrganizationMetadata.fromMap(organization.getMetadata());

    Member member =
        findMemberById(orgId, memberId)
            .orElseThrow(() -> VortexException.badRequest("Member not found"));

    User updateRequest = new User();

    if (updateMemberDto.getGivenName() != null || updateMemberDto.getFamilyName() != null) {

      // only support auth0 connection
      if (metadata.getStrategy() != ConnectionStrategyEnum.AUTH0) {
        throw VortexException.badRequest("Don't support update member info");
      }

      updateRequest.setGivenName(updateMemberDto.getGivenName());
      updateRequest.setFamilyName(updateMemberDto.getFamilyName());
      updateRequest.setName(
          String.format("%s %s", updateRequest.getGivenName(), updateRequest.getFamilyName()));
    }

    if (updateMemberDto.getBlocked() != null) {
      updateRequest.setBlocked(updateMemberDto.getBlocked());
    }

    try {
      return this.auth0Client
          .getMgmtClient()
          .users()
          .update(member.getUserId(), updateRequest)
          .execute()
          .getBody();
    } catch (Auth0Exception e) {
      throw badRequest(
          e, String.format("update member(%s) info for orgId:%s, error:", memberId, orgId));
    }
  }
}
