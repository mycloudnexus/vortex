package com.consoleconnect.vortex.iam.dto;

import lombok.Data;

@Data
public class UpdateConnectionDto {

  private OidcConnectionDto oidc;

  private SamlConnectionDto saml;
}
