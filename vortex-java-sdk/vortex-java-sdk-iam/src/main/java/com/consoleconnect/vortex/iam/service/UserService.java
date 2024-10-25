package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.UserInfo;
import com.consoleconnect.vortex.iam.mapper.UserMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
  private final Auth0Client auth0Client;

  public UserInfo getInfo(String userId) {
    try {
      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();

      UserInfo userInfo = UserMapper.INSTANCE.toUserInfo(user);
      userInfo.setOrganizations(organizations);
      return userInfo;
    } catch (Auth0Exception ex) {
      log.error("Failed to get user info", ex);
      throw VortexException.internalError("Failed to get user info");
    }
  }
}
