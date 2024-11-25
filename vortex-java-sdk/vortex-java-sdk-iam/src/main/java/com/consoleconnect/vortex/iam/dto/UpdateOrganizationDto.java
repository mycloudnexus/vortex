package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrganizationDto {
  @NotBlank
  @JsonProperty("display_name")
  @Size(max = 255, message = "Display name must be less than 255 characters")
  private String displayName;
}
