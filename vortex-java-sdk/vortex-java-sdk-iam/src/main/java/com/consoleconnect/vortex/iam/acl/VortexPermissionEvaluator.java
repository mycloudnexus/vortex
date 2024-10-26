package com.consoleconnect.vortex.iam.acl;

import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.service.PermissionService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class VortexPermissionEvaluator implements PermissionEvaluator {

  private final IamProperty iamProperty;
  private final PermissionService permissionService;

  @Override
  public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
    log.info(
        "Checking permission for user: {} to access:{}, required permission:{}",
        auth.getName(),
        targetDomainObject,
        permission);
    if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
      return false;
    }
    if (auth instanceof JwtAuthenticationToken) {
      return hasPrivilege(
          (JwtAuthenticationToken) auth,
          null,
          (targetDomainObject instanceof String)
              ? ((String) targetDomainObject).toUpperCase()
              : targetDomainObject.getClass().getSimpleName(),
          permission.toString());
    }
    log.error("Unknown authentication type: {}", auth.getClass().getSimpleName());
    return false;
  }

  @Override
  public boolean hasPermission(
      Authentication auth, Serializable targetId, String targetType, Object permission) {
    log.info(
        "Checking permission for user: {} to access:{}#{}, required permission:{}",
        auth.getName(),
        targetType,
        targetId,
        permission);
    if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
      return false;
    }

    if (auth instanceof JwtAuthenticationToken) {
      return hasPrivilege(
          (JwtAuthenticationToken) auth,
          targetId.toString(),
          targetType.toUpperCase(),
          permission.toString());
    }
    log.error("Unknown authentication type: {}", auth.getClass().getSimpleName());
    return false;
  }

  private boolean hasPrivilege(
      JwtAuthenticationToken jwtAuthenticationToken,
      String resourceId,
      String resource,
      String action) {

    String permissionKey = iamProperty.getJwt().getCustomClaims().getPermissions();
    String orgIdKey = iamProperty.getJwt().getCustomClaims().getOrgId();
    String rolesKey = iamProperty.getJwt().getCustomClaims().getRoles();
    Jwt jwt = (Jwt) jwtAuthenticationToken.getPrincipal();
    String orgId = jwt.getClaimAsString(orgIdKey);

    List<String> permissions = jwt.getClaimAsStringList(permissionKey);
    if (permissions == null) {
      log.warn("No {} found in the token", permissionKey);
      // load permissions from the role
      log.info("Loading permissions from the role");
      List<String> orgRoles = jwt.getClaimAsStringList(rolesKey);
      if (orgRoles == null || orgRoles.isEmpty()) {
        log.warn("No {} found in the token", rolesKey);
        return false;
      }
      permissions = new ArrayList<>();
      for (String role : orgRoles) {
        permissions.addAll(
            permissionService.listPermissions(role).stream().map(x -> x.getName()).toList());
      }
      log.info("Permissions loaded from the role: {}", permissions);
    }
    permissions = permissions.stream().map(String::toUpperCase).toList();
    String requiredPermission = String.format("%s:%s", resource, action).toUpperCase();
    if (resourceId == null) {
      return permissions.contains(requiredPermission);
    }

    if (orgId == null || orgId.isEmpty()) {
      log.warn("No {} found in the token", orgIdKey);
      return false;
    }
    return orgId.equalsIgnoreCase(resourceId) && permissions.contains(requiredPermission);
  }
}
