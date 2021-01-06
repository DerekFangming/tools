package com.tools.controller;

import com.tools.ToolsProperties;
import com.tools.domain.DiscordGuild;
import com.tools.domain.DiscordUser;
import com.tools.domain.DiscordUserLog;
import com.tools.dto.DiscordObjectDto;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.repository.DiscordUserRepo;
import com.tools.service.discord.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;


    @GetMapping("/members")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordUser> members() {
        return discordService.getMembers(toolsProperties.getDcDefaultGuildId());
    }

    @GetMapping("/members/sync")
    @PreAuthorize("hasRole('DC')")
    public List<String> sync(@RequestParam(value = "print", defaultValue="true") boolean print) {
        List<String> res = new ArrayList<>();
        List<DiscordUser> users = discordService.getMembers(toolsProperties.getDcDefaultGuildId());
        users.forEach(user -> {
            DiscordUser existingUser = discordUserRepo.findById(user.getId()).orElse(null);
            if (existingUser == null) {
                if (!print) {
                    discordUserRepo.save(user);
                }
                res.add(user.getId() + ":add:" + user.getNickname());
            } else {
                if (!print) {
                    existingUser.setNickname(user.getNickname());
                    existingUser.setRoles(user.getRoles());
                    existingUser.setAvatarId(user.getAvatarId());
                    existingUser.setJoinedDate(user.getJoinedDate());
                    existingUser.setCreatedDate(user.getCreatedDate());
                    existingUser.setBoostedDate(user.getBoostedDate());
                    discordUserRepo.save(existingUser);
                }
                res.add(user.getId() + ":update:" + user.getNickname());
            }
        });

        return res;
    }

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
    public DiscordGuild getConfig(@PathVariable("guildId") String guildId) {
        Optional<DiscordGuild> discordGuildOptional;
        if ("default".equalsIgnoreCase(guildId)) {
            discordGuildOptional = discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId());
        } else {
            discordGuildOptional = discordGuildRepo.findById(guildId);
        }

        return discordGuildOptional.orElseGet(() -> DiscordGuild.builder().build());
    }

    @PostMapping("/{guildId}/config")
    @PreAuthorize("hasRole('DC')")
    public DiscordGuild updateConfig(@PathVariable("guildId") String guildId, @RequestBody DiscordGuild discordGuild) {

        // Validate welcome settings
        if (StringUtils.isBlank(discordGuild.getId())) {
            throw new IllegalArgumentException("Id is required for configuration update.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeTitle())) {
            throw new IllegalArgumentException("Title for welcome message is required.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeDescription())) {
            throw new IllegalArgumentException("Description for welcome message is required.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeFooter())) {
            throw new IllegalArgumentException("Footer for welcome message is required.");
        } else if (discordGuild.isWelcomeEnabled() && discordGuild.getWelcomeChannelId() == null) {
            throw new IllegalArgumentException("Welcome announcement channel has to be set when welcome message is turned on.");
        }

        // Validate birthday settings
        if (discordGuild.isBirthdayEnabled()) {
            if (discordGuild.getBirthdayChannelId() == null) {
                throw new IllegalArgumentException("Birthday announcement channel has to be set when birthday blessing is turned on.");
            } else if (StringUtils.isBlank(discordGuild.getBirthdayMessage())) {
                throw new IllegalArgumentException("Birthday blessing message is required.");
            }
        }

        Optional<DiscordGuild> discordGuildOptional;
        if ("default".equalsIgnoreCase(guildId)) {
            discordGuildOptional = discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId());
        } else {
            discordGuildOptional = discordGuildRepo.findById(guildId);
        }

        if (discordGuildOptional.isPresent()) {
            discordGuildRepo.save(discordGuild);
            return discordGuild;
        } else {
            throw new IllegalArgumentException("Id is required for configuration update.");
        }
    }

}
