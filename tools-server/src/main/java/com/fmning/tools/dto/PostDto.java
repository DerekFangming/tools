package com.fmning.tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    int id;
    String title;
    List<String> imageNames;
    List<String> imageUrls;
    String attachment;
    int rank;
    int category;
    Boolean flagged;
    String url;
    Instant created;
    Boolean saved;
}
