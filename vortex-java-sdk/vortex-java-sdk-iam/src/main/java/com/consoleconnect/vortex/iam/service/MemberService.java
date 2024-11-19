package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.mapper.UserMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class MemberService {
  private final Auth0Client auth0Client;

  public MemberInfo getUserInfo(String userId) {
    try {
      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();
      MemberInfo memberInfo = UserMapper.INSTANCE.toMemberInfo(user);
      if (organizations != null && !organizations.isEmpty()) {
        if (organizations.size() > 1) {
          log.warn("User {} belongs to multiple organizations", userId);
        }
        Organization org = organizations.get(0);
        String orgId = org.getId();
        List<Role> roles =
            auth0Client
                .getMgmtClient()
                .organizations()
                .getRoles(orgId, userId, null)
                .execute()
                .getBody()
                .getItems();

        MemberInfo.UserOrganization userOrganization =
            JsonToolkit.fromJson(JsonToolkit.toJson(org), MemberInfo.UserOrganization.class);

        userOrganization.setRoles(roles);
        memberInfo.setOrganization(userOrganization);
      } else {
        log.warn("User {} does not belong to any organization", userId);
      }
      return memberInfo;
    } catch (Auth0Exception ex) {
      log.error("Failed to get user info", ex);
      throw VortexException.internalError("Failed to get user info");
    }
  }
}
