package com.consoleconnect.vortex.iam.model;

import com.sendgrid.helpers.mail.objects.Email;
import lombok.Data;

@Data
public class EmailProperty {
  private boolean enabled = false;
  private String provider = "sendgrid";

  private SendGrid sendGrid;

  @Data
  public static class SendGrid {
    private String apiKey;
    private Email from;
    private EmailTemplates templates;
  }

  @Data
  public static class EmailTemplates {
    private String inviteOrgMember;
  }
}
