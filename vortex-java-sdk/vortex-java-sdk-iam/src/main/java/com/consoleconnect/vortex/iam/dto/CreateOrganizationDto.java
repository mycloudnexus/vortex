package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrganizationDto {
  @NotBlank
  @Size(max = 20, message = "Name must be less than 20 characters")
  @Pattern(
      regexp = "^[a-z0-9][a-z0-9_-]*$",
      message =
          "Name must start with a letter or number and contain only letters, numbers, underscores, and hyphens")
  private String name;

  @NotBlank
  @Size(max = 255, message = "Display name must be less than 255 characters")
  @JsonProperty("display_name")
  private String displayName;

  private OrganizationMetadata metadata = new OrganizationMetadata();
}
