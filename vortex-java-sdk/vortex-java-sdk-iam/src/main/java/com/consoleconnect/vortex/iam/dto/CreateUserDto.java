package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CreateUserDto {
  @NotNull
  @Email(message = "Invalid email address.")
  private String email;

  private List<String> roles = List.of(RoleEnum.PLATFORM_MEMBER.toString());

  private boolean sendEmail = true;
}
