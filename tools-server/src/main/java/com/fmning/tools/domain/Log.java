package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="service")
    private String service;

    @Column(name="level")
    private String level;

    @Column(name="source")
    private String source;

    @Column(name="message")
    private String message;

    @Column(name="stacktrace")
    private String stacktrace;

    @Column(name="created")
    private Instant created;
}
