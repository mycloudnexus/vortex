package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import java.util.Map;
import lombok.Data;

@Data
public class SamlConnection {
  private String id;
  private String name;
  private String strategy = ConnectionStrategryEnum.SAML.getValue();
  private ConnectionOptions options;

  @Data
  public static class ConnectionOptions {
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
}
