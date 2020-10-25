package com.tools;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tools")
public class ToolsProperties {
    private String production;

    public boolean isProduction() {
        System.out.println(production);
        return Boolean.parseBoolean(production);
    }
}
