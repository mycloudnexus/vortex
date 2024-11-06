package com.consoleconnect.vortex.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateConnectionDto {

  @NotBlank private String id;

  private OidcConnectionDto odic;

  private SamlConnectionDto saml;
}
