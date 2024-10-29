package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.json.mgmt.organizations.*;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionService {

  @Async
  public void cleanConnectionAndMembers(
      ManagementAPI managementAPI, OrganizationsEntity organizationsEntity, String orgId) {
    log.info("cleanConnectionAndMembers, orgId={}", orgId);
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
