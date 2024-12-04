package com.consoleconnect.vortex.iam.model;

import lombok.Data;

@Data
public class IamProperty {
  private Auth0Property auth0;
  private ResourceServerProperty resourceServer;
  private EmailProperty email;
  private DownstreamProperty downStream;
}
