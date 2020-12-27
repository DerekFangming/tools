package com.tools.controller;

import com.tools.ToolsProperties;
import com.tools.dto.DiscordObjectDto;
import com.tools.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/discord")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;

    @GetMapping("/{guildId}/channels")
    public List<DiscordObjectDto> getChannels(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getTextChannels(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getTextChannels(guildId);
        }
    }

    @GetMapping("/{guildId}/roles")
    public List<DiscordObjectDto> getRoles(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getRoles(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getRoles(guildId);
        }
    }

}
