package com.consoleconnect.vortex.iam.dto;

import com.auth0.json.mgmt.permissions.Permission;
import com.auth0.json.mgmt.roles.Role;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleInfo extends Role {
  private List<Permission> permissions;
}
