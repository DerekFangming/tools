package com.tools.service;

import com.tools.domain.Email;
import com.tools.repository.EmailRepo;
import com.tools.service.emailsender.EmailSender;
import com.tools.service.emailsender.LocalSender;
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
        emailSender = new LocalSender();
    }

    public void send(Email email) {
        emailSender.send(email);
        emailRepo.save(email);
    }

}
