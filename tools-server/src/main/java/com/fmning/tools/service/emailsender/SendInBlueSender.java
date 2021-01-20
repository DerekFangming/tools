package com.fmning.tools.service.emailsender;

import com.fmning.tools.domain.Email;
import com.fmning.tools.type.EmailSenderType;
import lombok.extern.apachecommons.CommonsLog;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.SmtpApi;
import sibModel.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

@CommonsLog
public class SendInBlueSender implements EmailSender {

    int threshold;
    EmailSender delegate;

    public SendInBlueSender(EmailSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(Email email) {
        if (email.getSenderType() == EmailSenderType.SEND_IN_BLUE && isAbleToSend()) {
            try {
                ApiClient defaultClient = Configuration.getDefaultApiClient();
                ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
                apiKey.setApiKey(System.getenv("TL_SEND_IN_BLUE_API_KEY"));

                SmtpApi apiInstance = new SmtpApi();

                SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
                sendSmtpEmail.setSender(new SendSmtpEmailSender().email(email.getFrom()));
                sendSmtpEmail.setReplyTo(new SendSmtpEmailReplyTo().email(email.getFrom()));
                sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(email.getTo())));
                sendSmtpEmail.setSubject(email.getSubject());

                //Check for HTML content
                if (email.isHtml()) {
                    sendSmtpEmail.setHtmlContent(email.getContent());
                } else {
                    sendSmtpEmail.setTextContent(email.getContent());
                }

                //Check and add attachment
                if (email.getAttachment() != null) {
                    File attachment = new File(email.getAttachment());
                    sendSmtpEmail.setAttachment(Collections.singletonList(new SendSmtpEmailAttachment().name(attachment.getName()).content(Files.readAllBytes(attachment.toPath()))));
                }
                apiInstance.sendTransacEmail(sendSmtpEmail);
                log.info("Sent through send in blue");
            } catch (Exception e) {
                email.setError(e.getMessage());
                delegate.send(email);
            }
        } else {
            delegate.send(email);
        }
    }

    @Override
    public boolean isAbleToSend() {
        return threshold > 0;
    }

    @Override
    public void resetThreshold() {
        threshold = 300;
        delegate.resetThreshold();
    }
}
