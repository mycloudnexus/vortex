package com.consoleconnect.vortex.iam.auth0;

import static org.junit.jupiter.api.Assertions.*;

import com.auth0.client.auth.AuthAPI;
import com.consoleconnect.vortex.config.TestApplication;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class Auth0ClientTest {
  private Auth0Property auth0Property;
  private Auth0Client auth0Client;

  public Auth0ClientTest() {
    auth0Property = new Auth0Property();

    Auth0Property.Config mgmtApi = new Auth0Property.Config();
    mgmtApi.setClientId(UUID.randomUUID().toString());
    mgmtApi.setDomain("localhost");
    mgmtApi.setClientSecret(UUID.randomUUID().toString());
    mgmtApi.setAudience(UUID.randomUUID().toString());

    Auth0Property.Config app = new Auth0Property.Config();
    app.setClientId(UUID.randomUUID().toString());
    auth0Property.setApp(app);
    auth0Property.setMgmtApi(mgmtApi);
    auth0Client = new Auth0Client(auth0Property);
  }

  @Test
  void getAuthClient() {
    AuthAPI authAPI = auth0Client.getAuthClient();
    Assertions.assertThat(authAPI).isNotNull();
  }
}
