package com.fmning.tools.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordObjectDto {
    private String id;
    private String name;
}
