package com.consoleconnect.vortex.iam.mapper;

import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.iam.dto.OrganizationInfo;
import com.consoleconnect.vortex.iam.dto.OrganizationMetadata;

public class OrganizationMapper {
  private OrganizationMapper() {}

  public static final OrganizationMapper INSTANCE = new OrganizationMapper();

  public OrganizationInfo toOrganizationInfo(Organization organization) {
    OrganizationInfo organizationInfo = new OrganizationInfo();
    organizationInfo.setId(organization.getId());
    organizationInfo.setName(organization.getName());
    organizationInfo.setDisplayName(organization.getDisplayName());
    organizationInfo.setMetadata(OrganizationMetadata.fromMap(organization.getMetadata()));
    organizationInfo.setBranding(organization.getBranding());
    return organizationInfo;
  }
}
