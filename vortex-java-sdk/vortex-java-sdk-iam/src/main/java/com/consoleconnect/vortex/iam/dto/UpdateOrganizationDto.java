package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateOrganizationDto {
  @JsonProperty("display_name")
  private String displayName;
}
