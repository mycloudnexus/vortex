package com.consoleconnect.vortex.iam.enums;

public enum ConnectionStrategryEnum {
  OIDC("oidc"),
  SAML("saml"),
  OAUTH2("oauth2"),
  DB("auth0");

  private final String value;

  ConnectionStrategryEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
