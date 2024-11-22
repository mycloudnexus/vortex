package com.consoleconnect.vortex.iam.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Invitee;
import com.auth0.json.mgmt.organizations.Inviter;
import com.consoleconnect.vortex.core.exception.VortexException;
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
    iamProperty.setLoginUrl("http://localhost:3000");

    EmailProperty emailProperty = new EmailProperty();
    emailProperty.setEnabled(enabled);
    emailProperty.setProvider("sendgrid");

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

  @Test
  void givenEmailDisabled_whenSendEmail_thenDoNothing() {
    // given

    IamProperty iamProperty = createEmailConfiguration(false);
    EmailService emailService = Mockito.spy(new EmailService(iamProperty));
    EmailServiceMockHelper emailServiceMockHelper =
        new EmailServiceMockHelper(emailService, iamProperty);
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

    IamProperty iamProperty = createEmailConfiguration(true);
    EmailService emailService = Mockito.spy(new EmailService(iamProperty));
    EmailServiceMockHelper emailServiceMockHelper =
        new EmailServiceMockHelper(emailService, iamProperty);
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
              emailService.sendInvitation(invitation, false);
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
