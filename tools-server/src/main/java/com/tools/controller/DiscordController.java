package com.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.ToolsProperties;
import com.tools.domain.DiscordGuild;
import com.tools.domain.DiscordUserLog;
import com.tools.dto.DiscordConfigDto;
import com.tools.dto.DiscordObjectDto;
import com.tools.dto.DiscordWelcomeDto;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/discord")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;
    private final ObjectMapper objectMapper;
    private final DiscordGuildRepo discordGuildRepo;
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

    @GetMapping("/{guildId}/config")
    @PreAuthorize("hasRole('DC')")
    public DiscordConfigDto getConfig(@PathVariable("guildId") String guildId) throws JsonProcessingException {
//        Optional<DiscordGuild> discordWelcomeDtoOptional;
//        if ("default".equalsIgnoreCase(guildId)) {
//            discordWelcomeDtoOptional = discordGuildRepo.findById(Long.valueOf(toolsProperties.getDcDefaultGuildId()));
//        } else {
//            discordWelcomeDtoOptional = discordGuildRepo.findById(Long.valueOf(guildId));
//        }

//        if (discordWelcomeDtoOptional.isPresent()) {
//            DiscordWelcomeDto discordWelcomeDto = DiscordWelcomeDto.builder().build();
//            return DiscordConfigDto.builder()
//                    .id(discordWelcomeDtoOptional.get().getId())
//                    .welcomeEnabled(discordWelcomeDtoOptional.get().isWelcomeEnabled())
//                    .welcomeConfig(objectMapper.readValue(discordWelcomeDtoOptional.get().getWelcomeSetting(), DiscordWelcomeDto.class))
//                    .build();
//        }
        return DiscordConfigDto.builder().build();
    }

}
