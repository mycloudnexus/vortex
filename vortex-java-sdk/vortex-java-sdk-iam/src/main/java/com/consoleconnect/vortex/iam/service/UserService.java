package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.UsersEntity;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.permissions.Permission;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.RoleInfo;
import com.consoleconnect.vortex.iam.dto.UserInfo;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.mapper.UserMapper;
import com.consoleconnect.vortex.iam.model.IamProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
  private final Auth0Client auth0Client;
  private final PermissionService permissionService;
  private final IamProperty iamProperty;
  private final DownstreamRoleService downstreamRoleService;

  public Paging<Role> listRoles(int page, int size) {
    try {
      List<Role> items =
          auth0Client.getMgmtClient().roles().list(null).execute().getBody().getItems();
      return PagingHelper.toPage(items, page, size);
    } catch (Auth0Exception ex) {
      log.error("Failed to list roles", ex);
      throw VortexException.internalError("Failed to list roles");
    }
  }

  public RoleInfo getRoleById(String roleId) {
    try {
      Role role = auth0Client.getMgmtClient().roles().get(roleId).execute().getBody();
      List<Permission> permissions = permissionService.listPermissions(roleId);
      RoleInfo roleInfo = UserMapper.INSTANCE.toRoleInfo(role);
      roleInfo.setPermissions(permissions);
      return roleInfo;
    } catch (Auth0Exception ex) {
      log.error("Failed to get role", ex);
      throw VortexException.internalError("Failed to get role");
    }
  }

  public UserInfo getInfo(String userId) {
    try {
      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();
      UserInfo userInfo = UserMapper.INSTANCE.toUserInfo(user);
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

        UserInfo.UserOrganization userOrganization =
            JsonToolkit.fromJson(JsonToolkit.toJson(org), UserInfo.UserOrganization.class);

        userOrganization.setRoles(roles);
        userInfo.setOrganization(userOrganization);
      } else {
        log.warn("User {} does not belong to any organization", userId);
      }
      return userInfo;
    } catch (Auth0Exception ex) {
      log.error("Failed to get user info", ex);
      throw VortexException.internalError("Failed to get user info");
    }
  }

  public Paging<User> searchUsers(String q, String email, int page, int size) {
    try {
      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      List<User> items = null;
      if (email != null) {
        items = userEntity.listByEmail(email, null).execute().getBody();
      } else {
        UserFilter userFilter = new UserFilter();
        if (q != null) userFilter.withQuery(q);
        userFilter.withTotals(true);
        userFilter.withSearchEngine("v2");
        items = userEntity.list(userFilter).execute().getBody().getItems();
      }
      return PagingHelper.toPage(items, page, size);
    } catch (Auth0Exception ex) {
      log.error("Failed to search users", ex);
      throw VortexException.internalError("Failed to search users");
    }
  }

  public Map<String, Object> downstreamUserInfo(String userId, Jwt jwt) {
    try {

      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();
      Organization organization = organizations.get(0);
      List<String> resourceRoles =
          jwt.getClaimAsStringList(iamProperty.getJwt().getCustomClaims().getRoles());
      log.info(
          "downstream userinfo, user.email:{} orgId:{}", user.getEmail(), organization.getId());

      boolean mgmt = false;
      if (auth0Client.getAuth0Property().getMgmtOrgId().equalsIgnoreCase(organization.getId())
          || (resourceRoles.contains(RoleEnum.PLATFORM_ADMIN.name())
              || resourceRoles.contains(RoleEnum.PLATFORM_MEMBER.name()))) {
        mgmt = true;
      }

      return downstreamRoleService.getUserInfo(user.getEmail(), mgmt);
    } catch (Exception e) {
      log.error("downstream userinfo error", e);
      throw VortexException.badRequest("Retrieve downstream userinfo error.");
    }
  }
}
