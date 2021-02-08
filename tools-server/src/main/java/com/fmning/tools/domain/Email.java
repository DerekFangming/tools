package com.fmning.tools.domain;

import com.fmning.tools.type.EmailSenderType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name="tl_emails")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Email {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="from_addr")
    private String from;

    @Column(name="replacement_from_addr")
    private String replacementFrom;

    @Column(name="to_addr")
    private String to;

    @Column(name="subject")
    private String subject;

    @Column(name="content")
    private String content;

    @Column(name="html")
    private boolean html;

    @Column(name="attachment")
    private String attachment;

    @Column(name="sender_type")
    @Enumerated(EnumType.STRING)
    private EmailSenderType senderType;

    @Column(name="replacement_sender_type")
    @Enumerated(EnumType.STRING)
    private EmailSenderType replacementSenderType;

    @Column(name="request_address")
    private String requestAddress;

    @Type(type = "jsonb")
    @Column(name="request_headers", columnDefinition = "jsonb")
    private Map<String, String> requestHeaders;

    @Type(type = "jsonb")
    @Column(name="request_params", columnDefinition = "jsonb")
    private Map<String, String> requestParams;

    @Column(name="created")
    private Instant created;

    @Column(name="read")
    private boolean read;

    @Column(name="error")
    private String error;
}
