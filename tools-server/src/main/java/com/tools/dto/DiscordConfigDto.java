package com.tools.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordConfigDto {
    private long id;
    private boolean welcomeEnabled;
    private DiscordWelcomeDto welcomeConfig;
}
