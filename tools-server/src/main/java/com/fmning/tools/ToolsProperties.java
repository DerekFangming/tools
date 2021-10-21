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

    public String getTeamLimitChannelId() {
        return isProduction() ? "782427034327711765,790700339870433341,790475149324845056,826957188626513941" : "879561792194494524,900555475270119466";
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

    public String getMutedToleId() {
        return isProduction() ? "784919135494078474" : "895161398554935326";
    }
}
