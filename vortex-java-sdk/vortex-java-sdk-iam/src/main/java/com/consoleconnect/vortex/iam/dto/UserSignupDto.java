package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupDto {
  @NotBlank(message = "Password must not be blank.")
  @Size(min = 10, max = 20, message = "The length of password must between 10 to 20 characters.")
  @Pattern(
      regexp = "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=\\S+$).{10,20}$",
      message =
          "The password should contain at lest one lower letter, one upper letter and one number.")
  private String password;

  @JsonProperty("given_name")
  @NotBlank(message = "Given name must not be blank.")
  private String givenName;

  @JsonProperty("family_name")
  @NotBlank(message = "Family name must not be blank.")
  private String familyName;

  @NotBlank(message = "Organization must not be blank.")
  private String orgId;

  @NotBlank(message = "Invitation must not be blank.")
  private String invitationId;
}
