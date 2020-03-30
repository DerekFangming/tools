package com.tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostDto {
    int id;
    String title;
    List<String> imageNames;
}
