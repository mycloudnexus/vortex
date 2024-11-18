package com.consoleconnect.vortex.iam.dto;

import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberInfo extends User {
  private UserOrganization organization;

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class UserOrganization extends Organization {
    private List<Role> roles;
  }
}
