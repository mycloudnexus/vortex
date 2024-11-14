package com.consoleconnect.vortex.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.organizations.OrganizationsPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.auth0.net.Response;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.downstream.DownstreamUserInfo;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class UserServiceTest {
  @Autowired UserService userService;
  @SpyBean Auth0Client auth0Client;
  @SpyBean DownstreamRoleService downstreamRoleService;
  @SpyBean IamProperty iamProperty;
  private static final String ORG_ID = "org_xxxx";

  @Test
  void downstreamUserInfoPlatformAdmin() throws Auth0Exception {
    Jwt jwt = Mockito.mock(Jwt.class);
    List<String> roles = List.of(RoleEnum.PLATFORM_ADMIN.name());
    doReturn(roles).when(jwt).getClaimAsStringList(anyString());

    mockDownstreamUserInfo(true);

    DownstreamUserInfo userInfo = new DownstreamUserInfo();
    doReturn(userInfo).when(downstreamRoleService).getUserInfo(anyString(), anyBoolean());

    DownstreamUserInfo result = userService.downstreamUserInfo(UUID.randomUUID().toString(), jwt);
    assertEquals(userInfo, result);
  }

  @Test
  void downstreamUserInfoPlatformMember() throws Auth0Exception {
    Jwt jwt = Mockito.mock(Jwt.class);
    List<String> roles = List.of(RoleEnum.PLATFORM_MEMBER.name());
    doReturn(roles).when(jwt).getClaimAsStringList(anyString());

    mockDownstreamUserInfo(true);

    DownstreamUserInfo userInfo = new DownstreamUserInfo();
    doReturn(userInfo).when(downstreamRoleService).getUserInfo(anyString(), anyBoolean());

    DownstreamUserInfo result = userService.downstreamUserInfo(UUID.randomUUID().toString(), jwt);
    assertEquals(userInfo, result);
  }

  @Test
  void downstreamUserInfoNonMgmt() throws Auth0Exception {
    Jwt jwt = Mockito.mock(Jwt.class);
    List<String> roles = List.of(RoleEnum.ORG_ADMIN.name());
    doReturn(roles).when(jwt).getClaimAsStringList(anyString());

    mockDownstreamUserInfo(false);

    DownstreamUserInfo userInfo = new DownstreamUserInfo();
    doReturn(userInfo).when(downstreamRoleService).getUserInfo(anyString(), anyBoolean());

    DownstreamUserInfo result = userService.downstreamUserInfo(UUID.randomUUID().toString(), jwt);
    assertEquals(userInfo, result);
  }

  @Test
  void downstreamUserInfoException() {
    Jwt jwt = Mockito.mock(Jwt.class);
    List<String> roles = List.of(RoleEnum.ORG_ADMIN.name());
    doReturn(roles).when(jwt).getClaimAsStringList(anyString());

    doThrow(VortexException.badRequest("error"))
        .when(downstreamRoleService)
        .getUserInfo(anyString(), anyBoolean());

    assertThrows(
        Exception.class, () -> userService.downstreamUserInfo(UUID.randomUUID().toString(), jwt));
  }

  private void mockDownstreamUserInfo(boolean fixOrgId) throws Auth0Exception {
    ManagementAPI managementAPI = mock(ManagementAPI.class);
    Auth0Property auth0Property = mock(Auth0Property.class);
    doReturn(managementAPI).when(auth0Client).getMgmtClient();
    doReturn(auth0Property).when(auth0Client).getAuth0Property();
    doReturn(ORG_ID).when(auth0Property).getMgmtOrgId();

    DownstreamProperty downStreamProperty = mock(DownstreamProperty.class);
    doReturn(downStreamProperty).when(iamProperty).getDownStream();

    UsersEntity usersEntity = mock(UsersEntity.class);
    doReturn(usersEntity).when(managementAPI).users();

    Request<User> userRequest = mock(Request.class);
    Response<User> userResponse = mock(Response.class);
    doReturn(userRequest).when(usersEntity).get(anyString(), any());
    doReturn(userResponse).when(userRequest).execute();

    User user = mock(User.class);
    doReturn(user).when(userResponse).getBody();
    doReturn("test@example.com").when(user).getEmail();

    Request<OrganizationsPage> organizationRequest = mock(Request.class);
    doReturn(organizationRequest).when(usersEntity).getOrganizations(anyString(), any());

    Response<OrganizationsPage> organizationResponse = mock(Response.class);
    doReturn(organizationResponse).when(organizationRequest).execute();

    // mock query organization
    OrganizationsPage organizationsPage = mock(OrganizationsPage.class);
    doReturn(organizationsPage).when(organizationResponse).getBody();

    Organization organization = mock(Organization.class);
    List<Organization> organizations = mock(List.class);
    doReturn(organizations).when(organizationsPage).getItems();
    doReturn(organization).when(organizations).get(0);
    if (fixOrgId) {
      doReturn(ORG_ID).when(organization).getId();
    }
  }
}
