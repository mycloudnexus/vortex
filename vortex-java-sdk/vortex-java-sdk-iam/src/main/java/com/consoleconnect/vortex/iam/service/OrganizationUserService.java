package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.OrganizationsEntity;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.client.mgmt.filter.InvitationsFilter;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.json.mgmt.organizations.*;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.client.Auth0Client;
import com.consoleconnect.vortex.iam.model.UserResponse;
import com.consoleconnect.vortex.iam.model.UserSignUpReq;
import com.consoleconnect.vortex.iam.utils.DataMapper;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class OrganizationUserService {
  private static final String USER_CONNECTION_QUERY = "identities.connection:\"%s\"";
  private Auth0Client auth0Client;

  // username-and-password strategy
  private static final String CONNECTION_STRATEGY_PWD = "auth0";
  private static final String VALIDATION_REGEX =
      "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=\\S+$).{10,20}$";

  public OrganizationUserService(Auth0Client auth0Client) {
    this.auth0Client = auth0Client;
  }

  private ManagementAPI getMgmtAPI() {
    return this.auth0Client.getMgmtClient();
  }

  public String signUp(UserSignUpReq userSignUpReq) {
    try {
      // 1. Validate password
      validateEmailAndPwd(userSignUpReq.getPassword());
      ManagementAPI managementAPI = getMgmtAPI();
      UsersEntity usersEntity = managementAPI.users();

      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Organization organization =
          organizationsEntity.get(userSignUpReq.getOrganization()).execute().getBody();

      // 2. Validate whether the user is invited.
      InvitationsFilter invitationsFilter = new InvitationsFilter();
      invitationsFilter.withTotals(true);
      Request<InvitationsPage> invitationsPageRequest =
          organizationsEntity.getInvitations(organization.getId(), invitationsFilter);
      InvitationsPage invitationsPage = invitationsPageRequest.execute().getBody();
      if (Objects.isNull(invitationsPage) || CollectionUtils.isEmpty(invitationsPage.getItems())) {
        throw VortexException.badRequest("You are not invited.");
      }

      List<Invitation> filterInvitationList =
          invitationsPage.getItems().stream()
              .filter(
                  invitation -> invitation.getInvitee().getEmail().equals(userSignUpReq.getEmail()))
              .toList();
      if (CollectionUtils.isEmpty(filterInvitationList)) {
        throw VortexException.badRequest("You are not invited.");
      }

      // 3. Validate connection type.
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      EnabledConnection enabledConnection = enabledConnectionsPage.getItems().get(0);

      if (!CONNECTION_STRATEGY_PWD.equals(enabledConnection.getConnection().getStrategy())) {
        throw VortexException.badRequest("Don't support for you company.");
      }

      // 4. sign up in Auth0
      User user = new User();
      user.setId(UUID.randomUUID().toString().replace("-", ""));
      user.setPassword(userSignUpReq.getPassword().toCharArray());
      user.setEmail(userSignUpReq.getEmail());
      user.setFamilyName(userSignUpReq.getLastName());
      user.setGivenName(userSignUpReq.getFirstName());
      user.setConnection(enabledConnection.getConnection().getName());
      user = usersEntity.create(user).execute().getBody();

      organizationsEntity
          .addMembers(organization.getId(), new Members(List.of(user.getId())))
          .execute();
      return user.getId();
    } catch (APIException e) {
      log.error("[module-auth]signUp.api.error", e);
      throw VortexException.badRequest(e.getDescription());
    } catch (Exception e) {
      log.error("[module-auth]signUp.error", e);
      throw VortexException.badRequest(e.getMessage());
    }
  }

  public List<UserResponse> listByOrg(String shortName) {
    List<UserResponse> result = Lists.newArrayList();
    try {
      ManagementAPI managementAPI = getMgmtAPI();
      OrganizationsEntity organizationsEntity = managementAPI.organizations();
      Request<Organization> organizationFindRequest = organizationsEntity.getByName(shortName);
      Organization organization = organizationFindRequest.execute().getBody();

      // 1. Validate whether the company has bind connection.
      EnabledConnectionsPage enabledConnectionsPage =
          organizationsEntity.getConnections(organization.getId(), null).execute().getBody();
      if (Objects.isNull(enabledConnectionsPage)
          || CollectionUtils.isEmpty(enabledConnectionsPage.getItems())) {
        return Collections.emptyList();
      }

      // 2. members.
      PageFilter pageFilter = new PageFilter();
      pageFilter.withTotals(true);
      Request<MembersPage> membersPageRequest =
          organizationsEntity.getMembers(organization.getId(), pageFilter);
      MembersPage membersPage = membersPageRequest.execute().getBody();

      if (Objects.nonNull(membersPage) && !CollectionUtils.isEmpty(membersPage.getItems())) {
        Connection connection = enabledConnectionsPage.getItems().get(0).getConnection();
        UserFilter userFilter = new UserFilter();
        userFilter.withTotals(true);
        userFilter.withSearchEngine("v2");
        userFilter.withQuery(String.format(USER_CONNECTION_QUERY, connection.getName()));

        UsersEntity usersEntity = managementAPI.users();
        UsersPage usersPageResponse = usersEntity.list(userFilter).execute().getBody();
        result.addAll(DataMapper.INSTANCE.memberToUserResponses(usersPageResponse.getItems()));
      }

      // 3. invitations
      InvitationsFilter invitationsFilter = new InvitationsFilter();
      invitationsFilter.withTotals(true);
      Request<InvitationsPage> invitationsPageRequest =
          organizationsEntity.getInvitations(organization.getId(), invitationsFilter);
      InvitationsPage invitationsPage = invitationsPageRequest.execute().getBody();
      if (Objects.nonNull(invitationsPage)
          && !CollectionUtils.isEmpty(invitationsPage.getItems())) {
        List<Invitation> invitations = invitationsPage.getItems();
        result.addAll(DataMapper.INSTANCE.invitationToUserResponses(invitations));
      }
    } catch (Exception e) {
      log.error("[module-auth]getOrganizations.error", e);
    }

    return result;
  }

  public void validateEmailAndPwd(String pwd) {
    if (!pwd.matches(VALIDATION_REGEX)) {
      throw VortexException.badRequest(
          "The length of password must between 10 to 20. And it should contain at lest one lower letter, one upper letter and one number.");
    }
  }
}
