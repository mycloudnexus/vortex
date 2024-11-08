package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@AllArgsConstructor
public class DownstreamRoleService {
  private VortexServerConnector vortexServerConnector;
  private IamProperty iamProperty;

  @Async
  public void syncRole(String orgId, String username, String companyName) {
    if (!Objects.equals(orgId, iamProperty.getAuth0().getMgmtOrgId())) {
      return;
    }

    if (StringUtils.isBlank(username) || StringUtils.isBlank(companyName)) {
      throw VortexException.badRequest("Username or companyName cannot be empty.");
    }

    DownstreamProperty downStreamProperty = iamProperty.getDownStream();
    String url =
        String.format(
            downStreamProperty.getRoleEndpoint(),
            companyName,
            username,
            downStreamProperty.getRole());

    try {
      vortexServerConnector.put(
          downStreamProperty.getBaseUrl() + url,
          Map.of(downStreamProperty.getAdminApiKeyName(), downStreamProperty.getAdminApiKey()),
          null,
          new ParameterizedTypeReference<>() {});
    } catch (WebClientResponseException.Conflict webClientResponseException) {
      log.warn("Role has exist, username:{}", username);
    } catch (Exception e) {
      throw VortexException.badRequest("Add role error:" + e.getMessage());
    }
  }
}
