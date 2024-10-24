package com.consoleconnect.vortex.iam.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserInviteReq {
  @Email(message = "Not a validated invitee address.")
  private String inviteeEmail;

  @Email private String inviterEmail;

  @NotBlank private String companyId;
}
