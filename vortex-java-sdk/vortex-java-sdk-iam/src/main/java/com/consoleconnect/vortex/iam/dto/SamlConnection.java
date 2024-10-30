package com.consoleconnect.vortex.iam.dto;

import java.util.Map;
import lombok.Data;

@Data
public class SamlConnection {
  private String signInEndpoint;
  private String userIdAttribute;
  private String signingCert;

  private boolean debug;
  private boolean disableSignout;
  private String signOutEndpoint;

  private boolean signSAMLRequest;
  private String digestAlgorithm;
  private String signatureAlgorithm;
  private String protocolBinding;
  private Map<String, Object> fieldsMap;
}
