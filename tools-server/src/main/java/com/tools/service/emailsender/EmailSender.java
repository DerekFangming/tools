package com.tools.service.emailsender;

import com.tools.domain.Email;

public interface EmailSender {
    void send(Email email);
    boolean isAbleToSend();
}
