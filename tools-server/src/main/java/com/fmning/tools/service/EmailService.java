package com.fmning.tools.service;

import com.fmning.tools.service.emailsender.*;
import com.fmning.tools.domain.Email;
import com.fmning.tools.repository.EmailRepo;
import com.fmning.tools.type.EmailSenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class EmailService {

    EmailSender emailSender;
    private final EmailRepo emailRepo;

    @PostConstruct
    public void init() {
        emailSender = new MailGunSender(new SendInBlueSender(new SendGridSender(new GmailSender(new LocalSender()))));
        emailSender.resetThreshold();
    }

    public void send(Email email) {
        try {
            if (email.getSenderType() == null) {
                email.setSenderType(EmailSenderType.LOCAL);
            }
            emailSender.send(email);
        } catch (Exception e) {
            email.setError(e.getMessage());
        } finally {
            emailRepo.save(email);
        }
    }

    @Scheduled(cron="0 0 0 1 1/1 *")// First day of every month
    public void refreshAPIExpirationDate() {
        Email email = Email.builder()
                .from("admin@fmning.com")
                .to("synfm123@gmail.com")
                .subject("Monthly SIG refresh")
                .content(Instant.now().toString())
                .senderType(EmailSenderType.SEND_IN_BLUE)
                .created(Instant.now())
                .build();
        send(email);
    }

}
