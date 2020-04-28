package com.tools.dto;

import com.tools.type.EmailSenderType;
import lombok.Data;

@Data
public class EmailDto {
    private String from;
    private String to;
    private String subject;
    private String content;
    private boolean html;
    private EmailSenderType senderType;
}
