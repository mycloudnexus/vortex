package com.consoleconnect.vortex.iam.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateInivitationDto {
  private String email;
  private List<String> roles;
}
