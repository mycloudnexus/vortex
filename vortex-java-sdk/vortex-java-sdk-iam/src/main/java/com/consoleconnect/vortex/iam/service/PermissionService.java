package com.consoleconnect.vortex.iam.service;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.permissions.Permission;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class PermissionService {
  private final Auth0Client auth0Client;

  // TODO: implementing caching for permissions
  public List<Permission> listPermissions(String roleId) {
    try {
      return auth0Client
          .getMgmtClient()
          .roles()
          .listPermissions(roleId, null)
          .execute()
          .getBody()
          .getItems();
    } catch (Auth0Exception ex) {
      log.error("Failed to list roles", ex);
      throw VortexException.internalError("Failed to list roles");
    }
  }
}
