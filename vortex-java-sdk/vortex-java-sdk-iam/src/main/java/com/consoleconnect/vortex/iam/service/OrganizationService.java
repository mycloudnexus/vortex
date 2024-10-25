package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateInivitationDto;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class OrganizationService {

  private final Auth0Client auth0Client;

  public Organization create(CreateOrganizationDto request, String createdBy) {
    log.info("creating organization: {},requestedBy:{}", request, createdBy);
    try {
      OrganizationsEntity organizationsEntity = this.auth0Client.getMgmtClient().organizations();
      Organization organization = new Organization(request.getName());
      organization.setDisplayName(request.getDisplayName());
      Request<Organization> organizationRequest = organizationsEntity.create(organization);
      return organizationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("create organizations.error", e);
      throw VortexException.badRequest("create organizations.error" + e.getMessage());
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
      invitation.setRoles(getRoles(orgId, request));

      Request<Invitation> invitationRequest =
          organizationsEntity.createInvitation(orgId, invitation);
      return invitationRequest.execute().getBody();
    } catch (Auth0Exception e) {
      log.error("create invitations.error", e);
      throw VortexException.internalError("Failed to create invitations of organization: " + orgId);
    }
  }

  private Roles getRoles(String orgId, CreateInivitationDto request) {
    List<String> roleIds = new ArrayList<>();

    // check if the user is mgmt
    boolean isMgmt = orgId.equalsIgnoreCase(auth0Client.getAuth0Property().getMgmtOrgId());

    // assign mgmt role to the user
    if (isMgmt) {
      roleIds.add(auth0Client.getAuth0Property().getRoles().getMgmtRoleId());
    }

    // assign admin or user role to the user
    if (request.getRole() == RoleEnum.ADMIN) {
      roleIds.add(auth0Client.getAuth0Property().getRoles().getAdminRoleId());
    } else {
      roleIds.add(auth0Client.getAuth0Property().getRoles().getUserRoleId());
    }
    return new Roles(roleIds);
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
        List.of(
            auth0Client.getAuth0Property().getRoles().getAdminRoleId(),
            auth0Client.getAuth0Property().getRoles().getUserRoleId());
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
}
