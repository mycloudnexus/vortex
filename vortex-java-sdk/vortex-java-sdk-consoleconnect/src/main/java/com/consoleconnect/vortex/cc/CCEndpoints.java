package com.consoleconnect.vortex.cc;

public final class CCEndpoints {
  private CCEndpoints() {}

  public static final String UPDATE_MEMBER_ROLES = "/api/company/%s/memberships/%s/roles/%s";
  public static final String LIST_MEMBERS = "/v2/companies/%s/members?pageSize=%s";
  public static final String GET_USER_INFO_BY_ID = "/api/user/%s";
  public static final String GET_CURRENT_USER_INFO = "/api/token/auth";
}
