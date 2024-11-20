package com.consoleconnect.vortex.iam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.consoleconnect.vortex.cc.CCHttpClient;
import com.consoleconnect.vortex.cc.model.CCClientProperty;
import com.consoleconnect.vortex.cc.model.Member;
import com.consoleconnect.vortex.cc.model.Role;
import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.toolkit.GenericHttpClient;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class RoleServiceTest {
  private WebClient webClient = mock(WebClient.class);
  private GenericHttpClient genericHttpClient = new GenericHttpClient(webClient);
  private IamProperty iamProperty = mock(IamProperty.class);
  private CCClientProperty ccClientProperty = mock(CCClientProperty.class);
  private CCHttpClient ccHttpClient = new CCHttpClient(ccClientProperty, genericHttpClient);
  private static final String SYSTEM = "system";
  private static final String TEST_COMPANY = "test-company";

  private void mockAuth0Property(String uuid) {
    Auth0Property auth0 = new Auth0Property();
    doReturn(auth0).when(iamProperty).getAuth0();
  }

  private void mockRoleResponse() {
    Role role = new Role();
    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    doReturn(requestBodyUriSpec).when(webClient).method(HttpMethod.PUT);

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).accept(MediaType.APPLICATION_JSON);
    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).contentType(any());

    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
    doReturn(responseSpec).when(requestBodyUriSpec).retrieve();

    Mono memerMono = mock(Mono.class);
    doReturn(memerMono).when(responseSpec).bodyToMono(new ParameterizedTypeReference<>() {});

    doReturn(role).when(memerMono).block();
  }

  private void mockDownstreamProperty(String companyId) {
    DownstreamProperty downstreamProperty = new DownstreamProperty();
    downstreamProperty.setAdminApiKey(UUID.randomUUID().toString());
    downstreamProperty.setRole("role");
    downstreamProperty.setApiKeyName("Authorization");
    downstreamProperty.setAdminApiKey("Bearer ");
    downstreamProperty.setCompanyUsername(TEST_COMPANY);
    downstreamProperty.setBaseUrl("http://localhost");
    downstreamProperty.setCompanyId(
        StringUtils.isEmpty(companyId) ? UUID.randomUUID().toString() : companyId);
    doReturn(downstreamProperty).when(iamProperty).getDownStream();

    doReturn(UUID.randomUUID().toString()).when(ccClientProperty).getApiKeyName();
    doReturn(UUID.randomUUID().toString()).when(ccClientProperty).getAdminApiKey();
    doReturn("http://localhost").when(ccClientProperty).getBaseUrl();
    doReturn(UUID.randomUUID().toString()).when(ccClientProperty).getCompanyId();
    doReturn(TEST_COMPANY).when(ccClientProperty).getCompanyUsername();
  }

  @Test
  void testGetMemberByEmail() throws Exception {
    String downstreamCompanyId = UUID.randomUUID().toString();
    mockDownstreamProperty(downstreamCompanyId);

    String email = "test@example.com";
    Member downstreamMember = new Member();

    downstreamMember.setId(UUID.randomUUID().toString());
    downstreamMember.setUsername("test-user");
    downstreamMember.setEmail(email);

    Role role = new Role();
    role.setId(UUID.randomUUID().toString());
    role.setName("ADMIN");
    role.setDescription("ADMIN");

    Role.PolicyStatement statement = new Role.PolicyStatement();
    statement.setResource(List.of("*"));
    statement.setSid("Sid");
    statement.setEffect("Allow");
    statement.setAction(List.of("Create"));

    Role.PolicyDefinition policyDefinition = new Role.PolicyDefinition();
    policyDefinition.setStatement(List.of(statement));

    Role.DownstreamPolicy downstreamPolicy = new Role.DownstreamPolicy();
    downstreamPolicy.setName("test-policy");
    downstreamPolicy.setId(UUID.randomUUID().toString());
    downstreamPolicy.setDefinition(policyDefinition);

    role.setPolicies(List.of(downstreamPolicy));
    role.setPermissions(Map.of("create-actions", true));
    downstreamMember.setRoles(List.of(role));

    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    doReturn(requestBodyUriSpec).when(webClient).method(HttpMethod.GET);

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).accept(MediaType.APPLICATION_JSON);
    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).contentType(any());

    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
    doReturn(responseSpec).when(requestBodyUriSpec).retrieve();

    Mono<List<Member>> memerMono = mock(Mono.class);
    doReturn(memerMono)
        .when(responseSpec)
        .bodyToMono(new ParameterizedTypeReference<List<Member>>() {});

    CompletableFuture<List<Member>> completableFuture = mock(CompletableFuture.class);
    doReturn(completableFuture).when(memerMono).toFuture();
    doReturn(List.of(downstreamMember)).when(completableFuture).get();

    UserInfo userInfo = new UserInfo();
    userInfo.setLinkUserCompany(Map.of(downstreamCompanyId, new UserInfo.LinkUserCompany()));

    Mono<UserInfo> userInfoMono = mock(Mono.class);
    doReturn(userInfoMono)
        .when(responseSpec)
        .bodyToMono(new ParameterizedTypeReference<UserInfo>() {});

    CompletableFuture<UserInfo> userInfoFuture = mock(CompletableFuture.class);
    doReturn(userInfoFuture).when(userInfoMono).toFuture();
    doReturn(userInfo).when(userInfoFuture).get();

    UserInfo result = ccHttpClient.getUserInfo(email, true);
    assertThat(result).isNotNull();
  }
}
