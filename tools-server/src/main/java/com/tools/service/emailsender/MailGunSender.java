package com.tools.service.emailsender;

import com.tools.domain.Email;

public class MailGunSender implements EmailSender {
    @Override
    public void send(Email email) {

    }

    @Override
    public boolean isAbleToSend() {
        return false;
    }

    @Override
    public void resetThreshold() {

    }
}
