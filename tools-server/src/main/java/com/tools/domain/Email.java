package com.tools.domain;

import com.tools.type.EmailSenderType;
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
@Table(name="emails")
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
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    private String from;

    private String to;

    private String subject;

    private String content;

    private boolean html;

    @Column(name="sender")
    @Enumerated(EnumType.STRING)
    private EmailSenderType senderType;

    @Column(name="address")
    private String address;

    @Type(type = "jsonb")
    @Column(name="headers", columnDefinition = "jsonb")
    private Map<String, String> headers;

    @Type(type = "jsonb")
    @Column(name="parameters", columnDefinition = "jsonb")
    private Map<String, String> queryParams;

    @Column(name="created")
    private Instant created;

    @Column(name="error")
    private String error;
}
