package com.tools.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordWelcomeDto {
    private String title;
    private String description;
    private String thumbnail;
    private String footer;
    private long channelId;
    private long roleId;
}
