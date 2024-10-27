package com.consoleconnect.vortex.core.model;

import lombok.Data;

@Data
public class Email {
  private String provider = "SENDGRID";

  private String accessKey;
  private String from;
}
