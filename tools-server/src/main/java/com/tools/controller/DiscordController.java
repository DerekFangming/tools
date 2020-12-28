package com.tools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.ToolsProperties;
import com.tools.domain.DiscordUserLog;
import com.tools.dto.DiscordObjectDto;
import com.tools.dto.DiscordWelcomeDto;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/discord")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;
    private final ObjectMapper objectMapper;
    private final DiscordUserLogRepo discordUserLogRepo;

    @GetMapping("/{guildId}/channels")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordObjectDto> getChannels(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getTextChannels(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getTextChannels(guildId);
        }
    }

    @GetMapping("/{guildId}/roles")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordObjectDto> getRoles(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getRoles(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getRoles(guildId);
        }
    }

    @GetMapping("/{guildId}/user-logs")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordUserLog> getUserLogs(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordUserLogRepo.findAllByOrderByIdDesc();
        } else {
            return Collections.emptyList();
        }
    }

}
