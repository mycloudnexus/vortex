package com.consoleconnect.vortex.iam.dto.downstream;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class DownstreamUserInfo extends BaseUserInfo {
  private String type;
  private String status;
  private List<DownstreamCompany> companies;
  private Map<String, LinkUserCompany> linkUserCompany;

  @Data
  public static class LinkUserCompany {
    private String id;
    private String state;
    private String userId;
    private List<String> roleIds;
    private List<String> roles;
    private LinkUserCompanyPermission permissions;
  }

  @Data
  public static class LinkUserCompanyPermission {
    // <ADMIN,{company-update:false}>
    private Map<String, Map<String, Boolean>> groups;
    private List<DownstreamRole> roles;
  }

  @Data
  public static class DownstreamCompany {
    private String id;
    private String name;
    private String type;
    private String username;
    private String overviewImage;
    private List<String> categories;
    private CloudProviderDetail cloudProviderDetails;
  }

  @Data
  public static class CloudProviderDetail {
    private String bgpAuthority;
    private String companyType;
    private Asn asn;
  }

  @Data
  public static class Asn {
    private List<String> range;
  }
}
