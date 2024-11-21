package com.consoleconnect.vortex.cc.model;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfo extends BaseUserInfo {
  private String type;
  private String status;
  private List<Company> companies;
  private Map<String, LinkUserCompany> linkUserCompany;

  @Data
  public static class LinkUserCompany {
    private String id;
    private String state;
    private String userId;
    private List<String> roleIds;
    private List<String> roles;
  }

  @Data
  public static class Company {
    private String id;
    private String name;
    private String type;
    private String username;
  }
}
