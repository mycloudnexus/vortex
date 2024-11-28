package com.consoleconnect.vortex.iam.config;

import static org.mockito.ArgumentMatchers.eq;

import com.consoleconnect.vortex.core.model.AppProperty;
import com.consoleconnect.vortex.iam.service.EmailService;
import com.sendgrid.helpers.mail.objects.Email;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@AllArgsConstructor
public class EmailServiceMockHelper {
  private final EmailService emailService;
  private final AppProperty appProperty;

  public void setUp() {
    Mockito.doNothing().when(emailService).send(Mockito.any(), Mockito.any(), Mockito.any());
  }

  public void verifyInvitation(String email, String templateId, String inviterName) {
    ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);

    Mockito.verify(emailService, Mockito.times(1))
        .send(emailArgumentCaptor.capture(), eq(templateId), mapArgumentCaptor.capture());
    Assertions.assertEquals(email, emailArgumentCaptor.getValue().getEmail());

    Map<String, Object> context = mapArgumentCaptor.getValue();
    Assertions.assertEquals(inviterName, context.get("requestor"));
    Assertions.assertEquals(appProperty.getLoginUrl(), context.get("url"));
  }
}
