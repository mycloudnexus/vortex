package com.consoleconnect.vortex.iam.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CreateInvitationDto {
  @Email(message = "Invalid email address.")
  private String email;

  @NotEmpty(message = "Roles cannot be empty.")
  private List<String> roles;

  @Parameter(description = "Only for platform user.")
  private String username;

  private boolean sendEmail = true;
}
