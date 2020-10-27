package com.tools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tools.type.EmailSenderType;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class EmailDto {
    private String from = "admin@fmning.com";
    private String to = "synfm123@gmail.com";
    private String subject;
    private String content;
    private String attachment;
    private boolean html;
    private EmailSenderType senderType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String replacementFrom;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private EmailSenderType replacementSenderType;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String requestAddress;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Map<String, String> requestHeaders;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Map<String, String> requestParams;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean read;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String error;
}
