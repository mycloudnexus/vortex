package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.RoleEnum;
import lombok.Data;

@Data
public class CreateInivitationDto {
  private String email;
  private RoleEnum role;
}
