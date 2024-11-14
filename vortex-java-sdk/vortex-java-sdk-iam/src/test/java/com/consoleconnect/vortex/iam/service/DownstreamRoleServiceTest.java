package com.consoleconnect.vortex.iam.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.vortex.config.SyncTaskTestConfig;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.downstream.DownstreamMember;
import com.consoleconnect.vortex.iam.dto.downstream.DownstreamRole;
import com.consoleconnect.vortex.iam.dto.downstream.DownstreamUserInfo;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import io.micrometer.common.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Import({SyncTaskTestConfig.class})
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class DownstreamRoleServiceTest {
  @SpyBean private GenericHttpClient genericHttpClient;
  @SpyBean private IamProperty iamProperty;
  @Autowired private DownstreamRoleService downstreamRoleService;
  private static final String SYSTEM = "system";
  private static final String TEST_COMPANY = "Test Company";

  @Test
  void syncRole() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    mockDownstreamProperty(null);
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, SYSTEM);
    Assertions.assertThatNoException();
  }

  @Test
  void syncOrgNotSame() {
    mockAuth0Property(UUID.randomUUID().toString());
    mockDownstreamProperty(null);
    mockRoleResponse();
    downstreamRoleService.syncRole(UUID.randomUUID().toString(), SYSTEM);
    Assertions.assertThatNoException();
  }

  @Test
  void syncUsernameEmpty() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    mockDownstreamProperty(null);
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, null);
    Assertions.assertThatNoException();
  }

  @Test
  void syncRoleException() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    DownstreamProperty downstreamProperty = new DownstreamProperty();
    downstreamProperty.setAdminApiKey(UUID.randomUUID().toString());
    downstreamProperty.setRoleEndpoint("/");
    downstreamProperty.setRole("role");
    doReturn(downstreamProperty).when(iamProperty).getDownStream();
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, "test");
    Assertions.assertThatNoException();
  }

  private void mockRoleResponse() {
    DownstreamRole role = new DownstreamRole();
    doReturn(role).when(genericHttpClient).blockPut(anyString(), any(), any(), any());
  }

  private void mockAuth0Property(String uuid) {
    Auth0Property auth0 = new Auth0Property();
    auth0.setMgmtOrgId(uuid);
    doReturn(auth0).when(iamProperty).getAuth0();
  }

  private void mockDownstreamProperty(String companyId) {
    DownstreamProperty downstreamProperty = new DownstreamProperty();
    downstreamProperty.setAdminApiKey(UUID.randomUUID().toString());
    downstreamProperty.setRoleEndpoint("/");
    downstreamProperty.setRole("role");
    downstreamProperty.setApiKeyName("Authorization");
    downstreamProperty.setAdminApiKey("Bearer ");
    downstreamProperty.setCompanyUsername(TEST_COMPANY);
    downstreamProperty.setBaseUrl("http://localhost");
    downstreamProperty.setCompanyId(
        StringUtils.isEmpty(companyId) ? UUID.randomUUID().toString() : companyId);
    downstreamProperty.setMembersEndpoint("/v2/companies/%s/m?pageSize=%s");
    downstreamProperty.setUserInfoEndpoint("/api/user/info");
    doReturn(downstreamProperty).when(iamProperty).getDownStream();
  }

  @Test
  void testGetMemberByEmail() {
    String downstreamCompanyId = UUID.randomUUID().toString();
    mockDownstreamProperty(downstreamCompanyId);

    String email = "test@example.com";
    DownstreamMember downstreamMember = new DownstreamMember();

    downstreamMember.setId(UUID.randomUUID().toString());
    downstreamMember.setUsername("test-user");
    downstreamMember.setEmail(email);

    DownstreamRole role = new DownstreamRole();
    role.setId(UUID.randomUUID().toString());
    role.setName("ADMIN");
    role.setDescription("ADMIN");

    DownstreamRole.PolicyStatement statement = new DownstreamRole.PolicyStatement();
    statement.setResource(List.of("*"));
    statement.setSid("Sid");
    statement.setEffect("Allow");
    statement.setAction(List.of("Create"));

    DownstreamRole.PolicyDefinition policyDefinition = new DownstreamRole.PolicyDefinition();
    policyDefinition.setStatement(List.of(statement));

    DownstreamRole.DownstreamPolicy downstreamPolicy = new DownstreamRole.DownstreamPolicy();
    downstreamPolicy.setName("test-policy");
    downstreamPolicy.setId(UUID.randomUUID().toString());
    downstreamPolicy.setDefinition(policyDefinition);

    role.setPolicies(List.of(downstreamPolicy));
    role.setPermissions(Map.of("create-actions", true));
    downstreamMember.setRoles(List.of(role));
    doReturn(List.of(downstreamMember))
        .when(genericHttpClient)
        .unblockGet(
            anyString(),
            anyMap(),
            any(),
            Mockito.eq(new ParameterizedTypeReference<List<DownstreamMember>>() {}));

    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("linkUserCompany", Map.of(downstreamCompanyId, new HashMap<>()));
    doReturn(userInfo)
        .when(genericHttpClient)
        .unblockGet(
            anyString(),
            any(),
            any(),
            Mockito.eq(new ParameterizedTypeReference<Map<String, Object>>() {}));

    DownstreamUserInfo result = downstreamRoleService.getUserInfo(email, true);
    Assertions.assertThat(result).isNotNull();
  }
}
