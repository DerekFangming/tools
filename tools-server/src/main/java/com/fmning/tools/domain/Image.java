package com.fmning.tools.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fmning.tools.type.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_images")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Image {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private ImageType type;

    @Column(name="url")
    private String url;

    @Column(name="data")
    private String data;

    @Column(name="created")
    private Instant created;
}
