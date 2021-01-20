package com.fmning.tools;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tools")
public class ToolsProperties {

    private String production;
    private String dcBotToken;
    private String dcDefaultGuildId;
    private String youtubeApiKey;
    private String imgurClientId;

    public boolean isProduction() {
        return Boolean.parseBoolean(production);
    }
}
