package com.fmning.tools.domain;

import com.fmning.tools.type.HtmlReaderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.DynamicUpdate;


import jakarta.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Entity
@Table(name="tl_posts")
@DynamicUpdate
@Data
@Builder
@CommonsLog
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @Column(name="id")
    private int id;

    @Column(name="title")
    private String title;

    @Column(name="img_urls")
    private String imgUrls;

    @Column(name="attachment")
    private String attachment;

    @Column(name="html_reader_type")
    @Enumerated(EnumType.STRING)
    private HtmlReaderType htmlType;

    @Column(name="exception")
    private String exception;

    @Column(name="created")
    private Instant created;

    @Column(name="viewed")
    private Instant viewed;

    @Column(name="rank")
    private int rank;

    @Column(name="category")
    private int category;

    @Column(name="flagged")
    private Boolean flagged;

    @Column(name="saved")
    private Boolean saved;

    @Transient
    private String url;

    @Transient
    private boolean firstPage;

    @Transient
    private String html;

    public void logException(Exception e) {
        log.error("Post service ", e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logException(sw.toString());
    }

    public void logException(String s) {
        exception = exception == null ? s : exception + "\n\nNext:\n" + s;
    }

    public void logImageUrl(String i) {
        imgUrls = imgUrls == null ? i : imgUrls + "," + i;
    }

}
