package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.core.enums.OrgStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOrganizationDto {
  private String name;

  @JsonProperty("display_name")
  private String displayName;

  private OrgStatusEnum status;
}
