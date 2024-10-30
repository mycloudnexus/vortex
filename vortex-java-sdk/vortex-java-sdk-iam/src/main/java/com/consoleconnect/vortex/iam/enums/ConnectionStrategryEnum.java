package com.consoleconnect.vortex.iam.enums;

public enum ConnectionStrategryEnum {
  OIDC("oidc"),
  SAML("samlp"),
  OAUTH2("oauth2"),
  AUTH0("auth0");

  private final String value;

  ConnectionStrategryEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
