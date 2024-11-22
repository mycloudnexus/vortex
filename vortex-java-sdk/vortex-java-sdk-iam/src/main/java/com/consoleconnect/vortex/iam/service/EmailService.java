package com.consoleconnect.vortex.iam.service;

import com.auth0.json.mgmt.organizations.Invitation;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.model.EmailProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  private final SendGrid client;
  private final EmailProperty emailProperty;
  private final String loginUrl;

  public EmailService(IamProperty iamProperty) {
    this.emailProperty = iamProperty.getEmail();
    this.client = new SendGrid(emailProperty.getSendGrid().getApiKey());
    this.loginUrl = iamProperty.getLoginUrl();
  }

  public void sendInvitation(Invitation invitation, boolean isPlatformAdmin) {
    Email to = new Email(invitation.getInvitee().getEmail());
    String templateId =
        isPlatformAdmin
            ? emailProperty.getSendGrid().getTemplates().getUserInvitation()
            : emailProperty.getSendGrid().getTemplates().getOrgMemberInvitation();
    Map<String, Object> context = new HashMap<>();
    context.put("inviterName", invitation.getInviter().getName());
    context.put("vortexLoginUrl", loginUrl);
    send(to, templateId, context);
  }

  public void send(Email to, String templateId, Map<String, Object> context) {

    if (!emailProperty.isEnabled()) {
      log.info("Email service is disabled, skip sending email");
      return;
    }
    Mail mail = new Mail();
    mail.setFrom(emailProperty.getSendGrid().getFrom());
    mail.setTemplateId(templateId);

    Personalization personalization = new Personalization();
    context.forEach(personalization::addDynamicTemplateData);
    personalization.addTo(to);
    mail.addPersonalization(personalization);

    try {
      Request request = new Request();
      request.setBody(mail.build());
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      log.info("sending email to: {}", to.getEmail());
      Response res = client.api(request);
      log.info("email response, status: {}", res.getStatusCode());
      if (res.getStatusCode() >= 300) {
        log.error("Error occurs on sending email: {}", res.getBody());
        throw VortexException.internalError("Error occurs at sending email");
      }

    } catch (IOException ex) {
      log.error("Error occurs on sending email: ", ex);
      throw VortexException.internalError("Error occurs at sending email", ex);
    }
  }
}
