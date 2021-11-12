package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordRoleMapping;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.dto.DiscordAdminDto;
import com.fmning.tools.dto.DiscordRolePositionDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.service.discord.DiscordService;
import com.fmning.tools.type.DiscordRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@RestController
@RequestMapping(value = "/api/discord/admin")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordAdminController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;
    private final JDA jda;

    @GetMapping("/birthday")
    @PreAuthorize("hasRole('ADMIN')")
    public void runBirthday() {
        discordService.announceBirthDay();
    }

    @GetMapping("/sync-members")
    @PreAuthorize("hasRole('ADMIN')")
    public void syncMembers() {
        discordService.seedMembers(toolsProperties.getDcDefaultGuildId());
    }

    @GetMapping("/sync-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public void syncRoles() {
        discordService.seedRoles(toolsProperties.getDcDefaultGuildId());
    }

    @GetMapping("/sync-channels")
    @PreAuthorize("hasRole('ADMIN')")
    public void syncChannels() {
        discordService.seedChannels(toolsProperties.getDcDefaultGuildId());
    }

    @PostMapping("/add-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void addRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.addRole(discordAdminDto.getMemberId(), discordAdminDto.getRoleId());
    }

    @PostMapping("/remove-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.removeRole(discordAdminDto.getMemberId(), discordAdminDto.getRoleId());
    }

    @PostMapping("/move-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void moveRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.moveRole(discordAdminDto.getMemberId(), discordAdminDto.getPosition());
    }

    @GetMapping("/role-position")
    @PreAuthorize("hasRole('ADMIN')")
    public String position() {
        StringBuilder sb = new StringBuilder();
        List<DiscordRolePositionDto> positionDtos = discordRoleMappingRepo.findAllBoostRoleByPosition();
        for (DiscordRolePositionDto p : positionDtos) {
            DiscordRole role = discordRoleRepo.findById(p.getRoleId()).orElse(null);
            if (role != null) {
                sb.append(role.getPosition()).append(" = ").append(role.getName()).append(" = ").append(p.getRoleId())
                        .append(" = ").append(p.getOwnerName()).append(" = ").append(p.getOwnerBoostTime()).append("<br />");
            } else {
                sb.append("FAILED").append(" = ").append(p.getOwnerName()).append("<br />");
            }
        }

        return sb.toString();
    }

    @GetMapping("/role-fix-preview")
    @PreAuthorize("hasRole('ADMIN')")
    public String rolePreview(@RequestParam("roleId") String roleId) {
        List<DiscordUser> users = discordUserRepo.findByRolesContaining(roleId);

        StringBuilder existedSb = new StringBuilder();
        StringBuilder missingSb = new StringBuilder();
        DiscordRoleMapping owner = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, roleId);
        for (DiscordUser u : users) {
            if (!u.getId().equals(owner.getOwnerId())) {
                DiscordRoleMapping mapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(u.getId(), DiscordRoleType.SHARE, roleId);
                if (mapping == null) {
                    missingSb.append("Missing share to ").append(u.getNickname()).append("<br />");
                } else {
                    existedSb.append("Already have share to ").append(u.getNickname()).append("<br />");
                }
            }
        }

        return "Already existed mappings: <br /><br />" + existedSb.toString() + "<br /><br />" + "Missing mappings:<br />" + missingSb.toString();
    }

    @GetMapping("/role-fix")
    @PreAuthorize("hasRole('ADMIN')")
    public String roleFix() {
        String roleId = discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId()).get().getWelcomeRoleId();
        Role role = jda.getRoleById(roleId);
        Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
        if (role == null || guild == null) {
            return "no role or guild";
        }
        StringBuilder sb = new StringBuilder();

        Page<DiscordUser> page = discordUserRepo.findByRoles("[]", PageRequest.of(0, 100));

        for (DiscordUser u : page.getContent()) {
            sb.append("Adding ").append(u.getNickname()).append("<br />");
            guild.addRoleToMember(u.getId(), role).complete();
        }


        return sb.toString();
    }

    @GetMapping("/speed-on")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean startSpeed() {
        return discordService.startSpeed();
    }

    @GetMapping("/speed-off")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean stopSpeed() {
        return discordService.stopSpeed();
    }
}
