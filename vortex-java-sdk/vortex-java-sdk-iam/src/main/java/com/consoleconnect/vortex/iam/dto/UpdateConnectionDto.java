package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateConnectionDto {

  @NotBlank private String id;

  private ConnectionStrategryEnum strategy = ConnectionStrategryEnum.SAML;

  private OidcConnection odic;

  private SamlConnection saml;
}
