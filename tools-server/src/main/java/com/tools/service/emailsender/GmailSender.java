package com.tools.service.emailsender;

import com.tools.domain.Email;
import com.tools.type.EmailSenderType;
import lombok.extern.apachecommons.CommonsLog;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Properties;

@CommonsLog
public class GmailSender implements EmailSender {

    private static final String HOST = "smtp.gmail.com";
    private static final String GMAIL_PASSWORD = System.getenv("TL_GMAIL_PASSWORD");

    int accountOneThreshold;
    int accountTwoThreshold;

    EmailSender delegate;
    Properties properties;

    public GmailSender(EmailSender delegate) {
        this.delegate = delegate;

        properties = new Properties(System.getProperties());
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.password", GMAIL_PASSWORD);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
    }

    @Override
    public void send(Email email) {
        if (email.getSenderType() == EmailSenderType.GMAIL && isAbleToSend()) {
            String from;
            if (accountOneThreshold > 0) {
                accountOneThreshold --;
                from = "noreply.fmning@gmail.com";
            } else {
                accountTwoThreshold --;
                from = "service.fmning@gmail.com";
            }
            try {
                send(email, from);
                email.setReplacementFrom(from);
            } catch (Exception e) {
                email.setError(e.getMessage());
                delegate.send(email);
            }
        } else {
            delegate.send(email);
        }
    }

    private void send(Email email, String from) throws Exception {
        Properties emailProperties = new Properties(properties);
        emailProperties.put("mail.smtp.user", from);

        Session session = Session.getInstance(properties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        if (email.getTo().contains(",")) {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
        } else {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()));
        }
        message.setSubject(email.getSubject());

        //Check and add attachment
        if (email.getAttachment() != null) {
            Multipart multipart = new MimeMultipart();

            //Check for HTML content
            MimeBodyPart bodyPart = new MimeBodyPart();
            if (email.isHtml()) {
                bodyPart.setContent(email.getContent(), "text/html");
            } else {
                bodyPart.setText(email.getContent());
            }

            MimeBodyPart attachmentBodyPart= new MimeBodyPart();
            DataSource source = new FileDataSource(email.getAttachment());

            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(source.getName());

            multipart.addBodyPart(bodyPart);
            multipart.addBodyPart(attachmentBodyPart);
            message.setContent(multipart);

        } else {
            //Check for HTML content
            if (email.isHtml()) {
                message.setContent(email.getContent(), "text/html");
            } else {
                message.setText(email.getContent());
            }
        }

        Transport transport = session.getTransport("smtp");
        transport.connect(HOST, from, GMAIL_PASSWORD);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
        log.info("Sent through gmail");
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
