package com.consoleconnect.vortex.iam.dto;

import java.util.Map;
import lombok.Data;

@Data
public class SamlConnection {
  private String signInEndpoint;
  private String userIdAttribute;
  private String signingCert;

  private boolean debug = true;
  private boolean disableSignout;
  private String signOutEndpoint;

  private boolean signSAMLRequest = false;
  private String digestAlgorithm;
  private String signatureAlgorithm;
  private String protocolBinding;
  private Map<String, Object> fieldsMap;
}
