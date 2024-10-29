package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateOrganizationDto {
  @JsonProperty("display_name")
  private String displayName;

  private OrgStatusEnum status;
}
