package com.consoleconnect.vortex.iam.auth0;

import com.auth0.client.auth.AuthAPI;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
