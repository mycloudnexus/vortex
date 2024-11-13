package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.dto.DownstreamMember;
import com.consoleconnect.vortex.iam.dto.DownstreamRole;
import com.consoleconnect.vortex.iam.model.DownstreamProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Data
@Service
public class DownstreamRoleService {
  private GenericHttpClient genericHttpClient;
  private IamProperty iamProperty;

  private Cache<String, DownstreamMember> downstreamMemberLoadingCache =
      CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2, TimeUnit.MINUTES).build();

  public DownstreamRoleService(IamProperty iamProperty, GenericHttpClient vortexServerConnector) {
    this.iamProperty = iamProperty;
    this.genericHttpClient = vortexServerConnector;
  }

  @Async
  public void syncRole(String orgId, String username) {
    log.info("syncRole, orgId:{}, username:{}", orgId, username);
    if (!Objects.equals(orgId, iamProperty.getAuth0().getMgmtOrgId())) {
      return;
    }

    DownstreamProperty downStreamProperty = iamProperty.getDownStream();
    String url =
        String.format(
            downStreamProperty.getRoleEndpoint(),
            downStreamProperty.getCompanyUsername(),
            username,
            downStreamProperty.getRole());

    try {
      genericHttpClient.blockPut(
          downStreamProperty.getBaseUrl() + url,
          Map.of(downStreamProperty.getApiKeyName(), downStreamProperty.getAdminApiKey()),
          null,
          new ParameterizedTypeReference<>() {});
    } catch (WebClientResponseException.Conflict webClientResponseException) {
      log.warn("Role has exist, username:{}", username);
    } catch (Exception e) {
      throw VortexException.badRequest("Add role error:" + e.getMessage());
    }
  }

  public List<DownstreamMember> listCompanyMembers() {
    DownstreamProperty downStreamProperty = iamProperty.getDownStream();
    String url =
        String.format(
            downStreamProperty.getMembersEndpoint(), downStreamProperty.getCompanyId(), 0);
    List<DownstreamMember> members;
    try {
      members =
          genericHttpClient.unblockGet(
              downStreamProperty.getBaseUrl() + url,
              Map.of(downStreamProperty.getApiKeyName(), downStreamProperty.getAdminApiKey()),
              null,
              new ParameterizedTypeReference<List<DownstreamMember>>() {});
    } catch (Exception e) {
      throw VortexException.badRequest("List company members error:" + e.getMessage());
    }
    return members;
  }

  public DownstreamMember getMemberByEmail(String email) {
    try {
      return downstreamMemberLoadingCache.get(
          email,
          () -> {
            Optional<DownstreamMember> downstreamMemberOptional =
                listCompanyMembers().stream()
                    .filter(member -> member.getEmail().equalsIgnoreCase(email))
                    .findFirst();
            if (downstreamMemberOptional.isPresent()) {
              downstreamMemberLoadingCache.put(email, downstreamMemberOptional.get());
              return downstreamMemberOptional.get();
            }
            return null;
          });
    } catch (Exception e) {
      log.error("getOne error", e);
      throw VortexException.badRequest("Can't find one member.");
    }
  }

  public Map<String, Object> getUserInfo(String email, boolean mgmt) {
    DownstreamProperty downStreamProperty = iamProperty.getDownStream();

    if (!mgmt) {
      return genericHttpClient.unblockGet(
          downStreamProperty.getBaseUrl() + downStreamProperty.getUserAuthEndpoint(),
          Map.of(downStreamProperty.getApiKeyName(), downStreamProperty.getUserApiKey()),
          null,
          new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    DownstreamMember downstreamMember = getMemberByEmail(email);
    String url =
        String.format(downStreamProperty.getUserInfoEndpoint(), downstreamMember.getUsername());
    Map<String, Object> userInfo =
        genericHttpClient.unblockGet(
            downStreamProperty.getBaseUrl() + url,
            Map.of(downStreamProperty.getApiKeyName(), downStreamProperty.getAdminApiKey()),
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
    Map<String, Object> linkUserCompany =
        (Map<String, Object>) MapUtils.getMap(userInfo, "linkUserCompany");

    if (MapUtils.isNotEmpty(linkUserCompany)) {
      Map<String, Object> userCompany =
          (Map<String, Object>) MapUtils.getMap(linkUserCompany, downStreamProperty.getCompanyId());
      List<DownstreamRole> downstreamRoles = downstreamMember.getRoles();

      List<String> roleIds = new ArrayList<>(downstreamRoles.size());
      List<String> roleNames = new ArrayList<>(downstreamRoles.size());
      Map<String, Map<String, Boolean>> groups = new HashMap<>();
      for (DownstreamRole role : downstreamRoles) {
        roleIds.add(role.getId());
        roleNames.add(role.getName());
        groups.put(role.getName(), role.getPermissions());
      }

      userCompany.put("permissions", Map.of("roles", downstreamRoles, "groups", groups));
      userCompany.put("roleIds", roleIds);
      userCompany.put("roles", roleNames);
    }
    return userInfo;
  }
}
