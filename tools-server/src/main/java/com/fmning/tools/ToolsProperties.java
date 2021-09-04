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

    public String getApexChannelId() {
        return isProduction() ? "782427034327711765" : "879561792194494524";
    }
    public String getSelfServiceBotChannelId() {
        return isProduction() ? "788900934805225542" : "879561759776722945";
    }

    public String getYaofengNewbieRoleId() {
        return isProduction() ? "803212908896583731, 782484706754691072" : "792772398592163840";
    }

    public String getFont() {
        return isProduction() ? "Bradley Hand" : "Bradley Hand ITC";
    }
}
