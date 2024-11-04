package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConnectionDto {

  @NotBlank private String id;

  @NotNull private ConnectionStrategryEnum strategy;

  private OidcConnectionDto odic;

  private SamlConnectionDto saml;
}
