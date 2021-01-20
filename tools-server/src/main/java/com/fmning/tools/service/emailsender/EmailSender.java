package com.fmning.tools.service.emailsender;

import com.fmning.tools.domain.Email;

public interface EmailSender {
    void send(Email email);
    boolean isAbleToSend();
    void resetThreshold();
}
