package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import lombok.Data;

@Data
public class CreateConnectionDto {

  private String name;
  private ConnectionStrategryEnum strategy = ConnectionStrategryEnum.OIDC;
  private OidcConnection odic;
  private SamlConnection saml;
}
