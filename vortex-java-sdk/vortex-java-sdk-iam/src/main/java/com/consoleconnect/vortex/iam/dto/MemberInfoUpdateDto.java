package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberInfoUpdateDto {
  @NotBlank(message = "Given name can't be empty.")
  @JsonProperty("given_name")
  private String givenName;

  @NotBlank(message = "Family name can't be empty.")
  @JsonProperty("family_name")
  private String familyName;
}
