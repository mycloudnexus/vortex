package com.consoleconnect.vortex.iam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AttributeProperty {
  public Identifier identifier;

  @JsonProperty("profile_required")
  private boolean profileRequired;

  private Signup signup;

  @JsonProperty("verification_method")
  private String verificationMethod = "link";

  @Data
  public static class Signup {
    private String status;
    private Identifier verification;
  }

  @Data
  public static class Identifier {
    private boolean active;
  }
}
