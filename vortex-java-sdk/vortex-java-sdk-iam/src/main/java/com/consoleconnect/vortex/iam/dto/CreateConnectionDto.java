package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import lombok.Data;

@Data
public class CreateConnectionDto {
  private ConnectionStrategryEnum strategy = ConnectionStrategryEnum.OIDC;
  private OidcConnectionDto openID;
  private SamlConnectionDto saml;
}
