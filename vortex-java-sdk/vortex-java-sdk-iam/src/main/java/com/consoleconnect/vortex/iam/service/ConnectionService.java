package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.json.mgmt.organizations.*;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionService {

  @Async
  public void cleanConnectionAndMembers(
      ManagementAPI managementAPI,
      OrganizationsEntity organizationsEntity,
      EnabledConnection oldEnabledConnection,
      String orgId) {
    log.info("cleanConnectionAndMembers, orgId={}", orgId);
    try {
      Organization organization = organizationsEntity.get(orgId).execute().getBody();

      // delete members
      MembersPage membersPage =
          organizationsEntity.getMembers(organization.getId(), null).execute().getBody();
      if (Objects.nonNull(membersPage) && CollectionUtils.isNotEmpty(membersPage.getItems())) {
        List<Member> members = membersPage.getItems();
        managementAPI
            .organizations()
            .deleteMembers(orgId, new Members(members.stream().map(Member::getUserId).toList()))
            .execute();
      }

      // delete invitations
      InvitationsPage invitationsPage =
          organizationsEntity.getInvitations(organization.getId(), null).execute().getBody();
      if (Objects.nonNull(invitationsPage)
          && CollectionUtils.isNotEmpty(invitationsPage.getItems())) {
        List<Invitation> invitations = invitationsPage.getItems();
        for (Invitation invitation : invitations) {
          organizationsEntity.deleteInvitation(organization.getId(), invitation.getId()).execute();
        }
      }

      // delete connection
      managementAPI.connections().delete(oldEnabledConnection.getConnectionId()).execute();
    } catch (Exception e) {
      log.error("clean old members and connection error", e);
    }
  }
}
