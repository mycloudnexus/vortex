package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import lombok.Data;

@Data
public class CreateConnectionDto {
  private ConnectionStrategyEnum strategy = ConnectionStrategyEnum.OIDC;
  private OidcConnectionDto oidc;
  private SamlConnectionDto saml;
}
