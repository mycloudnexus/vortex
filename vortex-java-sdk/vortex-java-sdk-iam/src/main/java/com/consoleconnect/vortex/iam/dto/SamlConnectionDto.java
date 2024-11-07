package com.consoleconnect.vortex.iam.dto;

import java.util.Map;
import lombok.Data;

@Data
public class SamlConnectionDto {
  private String signInEndpoint;
  private String userIdAttribute;
  private String signingCert;

  private Boolean debug;
  private Boolean disableSignout;
  private String signOutEndpoint;

  private Boolean signSAMLRequest;
  private String digestAlgorithm;
  private String signatureAlgorithm;
  private String protocolBinding;
  private Map<String, Object> fieldsMap;
}
