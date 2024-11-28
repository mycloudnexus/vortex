package com.consoleconnect.vortex.iam.mapper;

import com.auth0.json.mgmt.users.Identity;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.iam.dto.MemberInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;

public class MemberMapper {
  private MemberMapper() {}

  public static final MemberMapper INSTANCE = new MemberMapper();

  public MemberInfo toMemberInfo(User user) {
    if (user == null) {
      return null;
    }

    MemberInfo memberInfo = new MemberInfo();

    memberInfo.setUsername(user.getUsername());
    memberInfo.setEmail(user.getEmail());
    memberInfo.setEmailVerified(user.isEmailVerified());
    memberInfo.setPhoneNumber(user.getPhoneNumber());
    memberInfo.setPhoneVerified(user.isPhoneVerified());
    memberInfo.setId(user.getId());
    memberInfo.setPicture(user.getPicture());
    memberInfo.setName(user.getName());
    memberInfo.setNickname(user.getNickname());
    memberInfo.setGivenName(user.getGivenName());
    memberInfo.setFamilyName(user.getFamilyName());
    Map<String, Object> map = user.getAppMetadata();
    if (map != null) {
      memberInfo.setAppMetadata(new LinkedHashMap<>(map));
    }
    Map<String, Object> map1 = user.getUserMetadata();
    if (map1 != null) {
      memberInfo.setUserMetadata(new LinkedHashMap<>(map1));
    }
    memberInfo.setBlocked(false);
    // By default, the value of blocked is null. Compliant solution from sonarcloud.
    if (BooleanUtils.isTrue(user.isBlocked())) {
      memberInfo.setBlocked(true);
    }
    if (memberInfo.getIdentities() != null) {
      List<Identity> list = user.getIdentities();
      if (list != null) {
        memberInfo.getIdentities().addAll(list);
      }
    }
    if (memberInfo.getMultifactor() != null) {
      List<String> list1 = user.getMultifactor();
      if (list1 != null) {
        memberInfo.getMultifactor().addAll(list1);
      }
    }
    if (memberInfo.getValues() != null) {
      Map<String, Object> map2 = user.getValues();
      if (map2 != null) {
        memberInfo.getValues().putAll(map2);
      }
    }

    return memberInfo;
  }
}
