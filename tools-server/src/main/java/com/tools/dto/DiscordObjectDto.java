package com.tools.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordObjectDto {
    private long id;
    private String name;
}
