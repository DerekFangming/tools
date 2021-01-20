package com.fmning.tools.service.emailsender;

import com.fmning.tools.domain.Email;
import com.fmning.tools.type.EmailSenderType;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@CommonsLog
public class SendGridSender implements EmailSender {

    int accountOneThreshold;
    int accountTwoThreshold;

    EmailSender delegate;

    public SendGridSender(EmailSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(Email email) {
        if (email.getSenderType() == EmailSenderType.SEND_GRID && isAbleToSend()) {
            String sendGridApiKey;
            if (accountOneThreshold > 0) {
                accountOneThreshold --;
                sendGridApiKey = System.getenv("TL_SEND_GRID_API_KEY_1");
            } else {
                accountTwoThreshold --;
                sendGridApiKey = System.getenv("TL_SEND_GRID_API_KEY_2");
            }
            try {
                send(email, sendGridApiKey);
            } catch (Exception e) {
                email.setError(e.getMessage());
                delegate.send(email);
            }
        } else {
            delegate.send(email);
        }
    }

    private void send(Email email, String sendGridApiKey) throws Exception {
        Mail mail = new Mail();
        mail.setFrom(new com.sendgrid.helpers.mail.objects.Email(email.getFrom()));
        Personalization personalization = new Personalization();
        String[] recipents = email.getTo().toString().split(",");
        for (String recipent : recipents)
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(recipent));

        mail.addPersonalization(personalization);
        mail.setSubject(email.getSubject());

        //Check for HTML content
        mail.addContent(new Content(email.isHtml() ? "text/html" : "text/plain", email.getContent()));

        //Check and add attachment
        if (email.getAttachment() != null) {
            File attachment = new File(email.getAttachment());
            byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(attachment));

            Attachments attachments = new Attachments();
            attachments.setContent(new String(encoded, StandardCharsets.US_ASCII));
            attachments.setType(Files.probeContentType(attachment.toPath()));
            attachments.setFilename(attachment.getName());
            attachments.setDisposition("attachment");
            mail.addAttachments(attachments);
        }

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Failed to send email with Send Grid" + response.toString());
        }
        log.info("Sent through send grid");
    }

    @Override
    public boolean isAbleToSend() {
        return accountOneThreshold > 0 || accountTwoThreshold > 0;
    }

    @Override
    public void resetThreshold() {
        accountOneThreshold = 100;
        accountTwoThreshold = 100;
        delegate.resetThreshold();
    }
}
