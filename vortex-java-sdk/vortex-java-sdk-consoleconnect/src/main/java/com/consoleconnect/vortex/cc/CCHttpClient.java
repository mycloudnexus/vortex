package com.consoleconnect.vortex.cc;

import com.consoleconnect.vortex.cc.model.CCClientProperty;
import com.consoleconnect.vortex.cc.model.Member;
import com.consoleconnect.vortex.cc.model.Role;
import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.GenericHttpClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
public class CCHttpClient {

  private final CCClientProperty clientProperty;
  private final GenericHttpClient genericHttpClient;

  private final Cache<String, Member> memberLoadingCache =
      CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2, TimeUnit.MINUTES).build();

  public CCHttpClient(CCClientProperty clientProperty, GenericHttpClient genericHttpClient) {
    this.clientProperty = clientProperty;
    this.genericHttpClient = genericHttpClient;
  }

  public void assignRole2Member(String username, String role) {

    String url =
        String.format(
            CCEndpoints.UPDATE_MEMBER_ROLES, clientProperty.getCompanyUsername(), username, role);

    try {
      genericHttpClient.blockPut(
          clientProperty.getBaseUrl() + url,
          Map.of(clientProperty.getApiKeyName(), clientProperty.getAdminApiKey()),
          null,
          new ParameterizedTypeReference<>() {});
    } catch (WebClientResponseException.Conflict webClientResponseException) {
      log.warn("Role has exist, username:{}", username);
    } catch (Exception e) {
      throw VortexException.badRequest("Add role error:" + e.getMessage());
    }
  }

  public List<Member> listCompanyMembers() {

    String url = String.format(CCEndpoints.LIST_MEMBERS, clientProperty.getCompanyId(), 0);
    List<Member> members;
    try {
      members =
          genericHttpClient.unblockGet(
              clientProperty.getBaseUrl() + url,
              Map.of(clientProperty.getApiKeyName(), clientProperty.getAdminApiKey()),
              null,
              new ParameterizedTypeReference<List<Member>>() {});
    } catch (Exception e) {
      throw VortexException.badRequest("List company members error:" + e.getMessage());
    }
    return members;
  }

  public Member getMemberByEmail(String email) {
    try {
      return memberLoadingCache.get(
          email,
          () -> {
            Optional<Member> downstreamMemberOptional =
                listCompanyMembers().stream()
                    .filter(member -> member.getEmail().equalsIgnoreCase(email))
                    .findFirst();
            if (downstreamMemberOptional.isPresent()) {
              memberLoadingCache.put(email, downstreamMemberOptional.get());
              return downstreamMemberOptional.get();
            }
            return null;
          });
    } catch (Exception e) {
      log.error("getOne error", e);
      throw VortexException.badRequest("Can't find one member.");
    }
  }

  public UserInfo getUserInfo(String email, boolean mgmt) {

    if (!mgmt) {
      return genericHttpClient.unblockGet(
          clientProperty.getBaseUrl() + CCEndpoints.GET_CURRENT_USER_INFO,
          Map.of(clientProperty.getApiKeyName(), clientProperty.getUserApiKey()),
          null,
          new ParameterizedTypeReference<UserInfo>() {});
    }

    Member member = getMemberByEmail(email);
    String url = String.format(CCEndpoints.GET_USER_INFO_BY_ID, member.getUsername());
    UserInfo userInfo =
        genericHttpClient.unblockGet(
            clientProperty.getBaseUrl() + url,
            Map.of(clientProperty.getApiKeyName(), clientProperty.getAdminApiKey()),
            null,
            new ParameterizedTypeReference<UserInfo>() {});

    Map<String, UserInfo.LinkUserCompany> linkUserCompany =
        MapUtils.isEmpty(userInfo.getLinkUserCompany())
            ? new HashMap<>()
            : userInfo.getLinkUserCompany();
    UserInfo.LinkUserCompany userCompany =
        linkUserCompany.getOrDefault(clientProperty.getCompanyId(), new UserInfo.LinkUserCompany());
    List<Role> roles = member.getRoles();

    List<String> roleIds = new ArrayList<>(roles.size());
    List<String> roleNames = new ArrayList<>(roles.size());
    Map<String, Map<String, Boolean>> groups = new HashMap<>();
    for (Role role : roles) {
      roleIds.add(role.getId());
      roleNames.add(role.getName());
      groups.put(role.getName(), role.getPermissions());
    }

    UserInfo.LinkUserCompanyPermission linkUserCompanyPermission =
        new UserInfo.LinkUserCompanyPermission();
    linkUserCompanyPermission.setGroups(groups);
    linkUserCompanyPermission.setRoles(roles);

    userCompany.setPermissions(linkUserCompanyPermission);
    userCompany.setRoleIds(roleIds);
    userCompany.setRoles(roleNames);
    return userInfo;
  }
}
