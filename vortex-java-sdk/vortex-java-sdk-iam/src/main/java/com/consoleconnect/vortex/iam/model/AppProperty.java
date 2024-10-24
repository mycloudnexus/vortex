package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class AppProperty {

  private String resellerCompany;

  private AuthConfig auth;

  private CcConfig cc;
}
