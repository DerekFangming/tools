package com.tools.service;

import com.tools.domain.Email;
import com.tools.repository.EmailRepo;
import com.tools.service.emailsender.EmailSender;
import com.tools.service.emailsender.GmailSender;
import com.tools.service.emailsender.LocalSender;
import com.tools.type.EmailSenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class EmailService {

    EmailSender emailSender;
    private final EmailRepo emailRepo;

    @PostConstruct
    public void init() {
        emailSender = new GmailSender(new LocalSender());
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

}
