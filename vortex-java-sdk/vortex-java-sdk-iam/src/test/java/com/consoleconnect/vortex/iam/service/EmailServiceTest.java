package com.consoleconnect.vortex.iam.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Invitee;
import com.auth0.json.mgmt.organizations.Inviter;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.model.AppProperty;
import com.consoleconnect.vortex.iam.config.EmailServiceMockHelper;
import com.consoleconnect.vortex.iam.model.EmailProperty;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.sendgrid.helpers.mail.objects.Email;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EmailServiceTest {

  IamProperty createEmailConfiguration(boolean enabled) {
    IamProperty iamProperty = new IamProperty();

    EmailProperty emailProperty = new EmailProperty();
    emailProperty.setEnabled(enabled);
    emailProperty.setProvider("sendgrid");
    emailProperty.setSupportEmail("support@support.com");

    EmailProperty.SendGrid sg = new EmailProperty.SendGrid();
    sg.setApiKey(UUID.randomUUID().toString());
    sg.setFrom(new Email("from@from.com"));

    EmailProperty.EmailTemplates templates = new EmailProperty.EmailTemplates();
    templates.setOrgMemberInvitation(UUID.randomUUID().toString());
    templates.setUserInvitation(UUID.randomUUID().toString());
    sg.setTemplates(templates);

    emailProperty.setSendGrid(sg);
    iamProperty.setEmail(emailProperty);
    return iamProperty;
  }

  AppProperty createAppConfiguration() {
    AppProperty appProperty = new AppProperty();
    appProperty.setLoginUrl("http://localhost:3000");
    appProperty.setProductName("Vortex");
    return appProperty;
  }

  @Test
  void givenEmailDisabled_whenSendEmail_thenDoNothing() {
    // given
    IamProperty iamProperty = createEmailConfiguration(false);
    AppProperty appProperty = createAppConfiguration();
    EmailService emailService = Mockito.spy(new EmailService(iamProperty, appProperty));
    EmailServiceMockHelper emailServiceMockHelper =
        new EmailServiceMockHelper(emailService, appProperty);
    // when
    Inviter inviter = new Inviter("This is a inviter");
    Invitee invitee = new Invitee("to@fake.com");
    Invitation invitation = new Invitation(inviter, invitee, "auth0-client-id");
    emailService.sendInvitation(invitation, true);

    emailServiceMockHelper.verifyInvitation(
        invitee.getEmail(),
        iamProperty.getEmail().getSendGrid().getTemplates().getUserInvitation(),
        inviter.getName());

    emailService.sendInvitation(invitation, false);
    emailServiceMockHelper.verifyInvitation(
        invitee.getEmail(),
        iamProperty.getEmail().getSendGrid().getTemplates().getOrgMemberInvitation(),
        inviter.getName());
  }

  @Test
  void givenEmailEnabled_whenSendEmail_thenEmailSent() {
    // given
    AppProperty appProperty = createAppConfiguration();
    IamProperty iamProperty = createEmailConfiguration(true);
    EmailService emailService = Mockito.spy(new EmailService(iamProperty, appProperty));
    EmailServiceMockHelper emailServiceMockHelper =
        new EmailServiceMockHelper(emailService, appProperty);
    // when
    Inviter inviter = new Inviter("This is a inviter");
    Invitee invitee = new Invitee("to@fake.com");
    Invitation invitation = new Invitation(inviter, invitee, "auth0-client-id");

    VortexException exception =
        assertThrows(
            VortexException.class,
            () -> {
              emailService.sendInvitation(invitation, true);
            });

    // sendgrid client will throw exception as the API key is not valid
    String errorMessage = "The provided authorization grant is invalid, expired, or revoked";
    Assertions.assertEquals(500, exception.getCode());
    Assertions.assertTrue(exception.getMessage().contains(errorMessage));
    emailServiceMockHelper.verifyInvitation(
        invitee.getEmail(),
        iamProperty.getEmail().getSendGrid().getTemplates().getUserInvitation(),
        inviter.getName());

    exception =
        assertThrows(
            VortexException.class,
            () -> {
              emailService.sendInvitation(invitation, "fakeName", false);
            });

    // sendgrid client will throw exception as the API key is not valid
    Assertions.assertEquals(500, exception.getCode());
    Assertions.assertTrue(exception.getMessage().contains(errorMessage));
    emailServiceMockHelper.verifyInvitation(
        invitee.getEmail(),
        iamProperty.getEmail().getSendGrid().getTemplates().getOrgMemberInvitation(),
        inviter.getName());
  }
}
