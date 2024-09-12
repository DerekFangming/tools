package com.fmning.tools.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Builder
public class DocumentDto {
    private int id;
    private String name;
    private String owner;
    private Date expirationDate;
    private List<String> images;
}
