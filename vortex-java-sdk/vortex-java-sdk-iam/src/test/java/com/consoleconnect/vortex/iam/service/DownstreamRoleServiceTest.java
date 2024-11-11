package com.consoleconnect.vortex.iam.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.consoleconnect.vortex.config.SyncTaskTestConfig;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.iam.dto.DownstreamRole;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Import({SyncTaskTestConfig.class})
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class DownstreamRoleServiceTest {
  @SpyBean private GenericHttpClient vortexServerConnector;
  @SpyBean private IamProperty iamProperty;
  @Autowired private DownstreamRoleService downstreamRoleService;
  private static final String SYSTEM = "system";
  private static final String TEST_COMPANY = "Test Company";

  @Test
  void syncRole() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    mockDownstreamProperty();
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, SYSTEM, TEST_COMPANY);
    Assertions.assertThatNoException();
  }

  @Test
  void syncOrgNotSame() {
    mockAuth0Property(UUID.randomUUID().toString());
    mockDownstreamProperty();
    mockRoleResponse();
    downstreamRoleService.syncRole(UUID.randomUUID().toString(), SYSTEM, TEST_COMPANY);
    Assertions.assertThatNoException();
  }

  @Test
  void syncUsernameEmpty() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    mockDownstreamProperty();
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, null, TEST_COMPANY);
    Assertions.assertThatNoException();
  }

  @Test
  void syncCompanyEmpty() {
    String uuid = UUID.randomUUID().toString();
    mockAuth0Property(uuid);
    mockDownstreamProperty();
    mockRoleResponse();
    downstreamRoleService.syncRole(uuid, SYSTEM, null);
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
    downstreamRoleService.syncRole(uuid, "test", "Test Company");
    Assertions.assertThatNoException();
  }

  private void mockRoleResponse() {
    DownstreamRole role = new DownstreamRole();
    doReturn(role).when(vortexServerConnector).put(anyString(), any(), any(), any());
  }

  private void mockAuth0Property(String uuid) {
    Auth0Property auth0 = new Auth0Property();
    auth0.setMgmtOrgId(uuid);
    doReturn(auth0).when(iamProperty).getAuth0();
  }

  private void mockDownstreamProperty() {
    DownstreamProperty downstreamProperty = new DownstreamProperty();
    downstreamProperty.setAdminApiKey(UUID.randomUUID().toString());
    downstreamProperty.setRoleEndpoint("/");
    downstreamProperty.setRole("role");
    downstreamProperty.setAdminApiKeyName("Authorization");
    downstreamProperty.setAdminApiKey("Bearer ");
    downstreamProperty.setBaseUrl("http://localhost");
    doReturn(downstreamProperty).when(iamProperty).getDownStream();
  }
}
