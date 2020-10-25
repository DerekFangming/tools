package com.tools;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sp-slack-integration")
public class ToolsProperties {
    private String production;

    public boolean isProduction() {
        return Boolean.parseBoolean(production);
    }
}
