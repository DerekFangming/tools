package com.tools.service.emailsender;

import com.tools.domain.Email;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LocalSender implements EmailSender {

    @Override
    public void send(Email email) {
    }

    @Override
    public boolean isAbleToSend() {
        return true;
    }
}
