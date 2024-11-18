package com.consoleconnect.vortex.iam.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CreateUserDto {

  @NotNull private String userId;

  private List<String> roles;

  private boolean sendEmail = true;
}
