package com.tools.domain;

import com.tools.type.HtmlReaderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="posts")
@DynamicUpdate
@Data
@Builder
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

    @Column(name="html")
    private String html;

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
    private boolean flagged;

    @Transient
    private String url;

    @Transient
    private boolean firstPage;

    public void logException(Exception e) {
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
