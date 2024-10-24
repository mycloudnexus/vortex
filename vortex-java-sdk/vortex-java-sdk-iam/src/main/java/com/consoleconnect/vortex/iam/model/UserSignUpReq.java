package com.consoleconnect.vortex.iam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSignUpReq {
  @Email(message = "Not a validated email address.")
  private String email;

  @NotBlank private String password;

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  // Callback field from Auth0(organization id)
  private String organization;

  // Callback field from Auth0(whitelabel: company short name)
  @NotBlank
  @JsonProperty("organization_name")
  private String organizationName;

  private String invitation;
}
