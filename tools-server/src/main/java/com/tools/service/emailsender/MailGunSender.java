package com.tools.service.emailsender;

import com.tools.domain.Email;
import com.tools.type.EmailSenderType;
import lombok.extern.apachecommons.CommonsLog;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailBuilder;
import net.sargue.mailgun.Response;

import java.io.File;

@CommonsLog
public class MailGunSender implements EmailSender {

    private static final String FROM_ADDRESS = "admin@fmning.com";

    int threshold;
    EmailSender delegate;

    public MailGunSender(EmailSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(Email email) {
        if (email.getSenderType() == EmailSenderType.MAIL_GUN && isAbleToSend()) {
            try {
                Configuration configuration = new Configuration()
                        .domain("fmning.com")
                        .apiKey(System.getenv("MAIL_GUN_API_KEY"))
                        .from(FROM_ADDRESS);

                MailBuilder builder = Mail.using(configuration);
                Response response;
                builder.to(email.getTo()).subject(email.getSubject());

                //Check for HTML content
                if (email.isHtml()) {
                    builder.html(email.getContent());
                } else {
                    builder.text(email.getContent());
                }

                //Check and add attachment
                if (email.getAttachment() != null) {
                    File attachment = new File(email.getAttachment());
                    response = builder.multipart().attachment(attachment).build().send();
                } else {
                    response = builder.build().send();
                }
                if (response.responseType() != Response.ResponseType.OK) {
                    throw new IllegalStateException("Failed to send email with Mail Gun" + response.responseMessage());
                }

                email.setReplacement_from(FROM_ADDRESS);
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
        threshold = 50;
        delegate.resetThreshold();
    }
}
