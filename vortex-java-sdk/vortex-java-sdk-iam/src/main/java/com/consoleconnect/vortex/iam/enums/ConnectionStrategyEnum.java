package com.consoleconnect.vortex.iam.enums;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum ConnectionStrategyEnum {
  OIDC("oidc"),
  SAML("samlp"),
  OAUTH2("oauth2"),
  AUTH0("auth0"),
  UNDEFINED("undefined");

  private final String value;

  ConnectionStrategyEnum(String value) {
    this.value = value;
  }

  /**
   * @JsonValue The enum type will be converted to value in Swagger and json properties. eg: OIDC ->
   * oidc;
   *
   * @return
   */
  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * @JsonCreator Controller can accept value field as parameter and convert to Enum. eg: oidc ->
   * OIDC;
   *
   * @param value
   * @return
   */
  @JsonCreator
  public static ConnectionStrategyEnum from(String value) {
    Optional<ConnectionStrategyEnum> enumOptional =
        Arrays.stream(ConnectionStrategyEnum.values())
            .filter(e -> e.value.equalsIgnoreCase(value))
            .findFirst();
    if (enumOptional.isPresent()) {
      return enumOptional.get();
    }
    throw VortexException.badRequest("Unknown value:" + value);
  }
}
