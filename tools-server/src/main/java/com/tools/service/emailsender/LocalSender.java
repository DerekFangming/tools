package com.tools.service.emailsender;

import com.tools.domain.Email;
import com.tools.type.EmailSenderType;
import lombok.NoArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

@NoArgsConstructor
@CommonsLog
public class LocalSender implements EmailSender {

    @Override
    public void send(Email email) {
        if (email.getSenderType() != EmailSenderType.LOCAL) {
            email.setReplacementSenderType(EmailSenderType.LOCAL);
        }
        log.info("Sent through local");
    }

    @Override
    public boolean isAbleToSend() {
        return true;
    }

    @Override
    public void resetThreshold() {
    }
}
