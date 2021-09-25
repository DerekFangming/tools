package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordRoleMapping;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.dto.DiscordAdminDto;
import com.fmning.tools.dto.DiscordRolePositionDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.service.discord.DiscordService;
import com.fmning.tools.type.DiscordRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/discord/admin")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordAdminController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;

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
    public String roleFix(@RequestParam("roleId") String roleId) {
        List<DiscordUser> users = discordUserRepo.findByRolesContaining(roleId);
        StringBuilder sb = new StringBuilder();
        DiscordRoleMapping owner = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, roleId);
        for (DiscordUser u : users) {
            if (!u.getId().equals(owner.getOwnerId())) {
                DiscordRoleMapping mapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(u.getId(), DiscordRoleType.SHARE, roleId);
                if (mapping == null) {
                    sb.append("Added share to ").append(u.getNickname()).append("<br />");
                    discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                            .guildId(toolsProperties.getDcDefaultGuildId())
                            .roleId(roleId)
                            .enabled(true)
                            .code(RandomStringUtils.randomAlphanumeric(6))
                            .type(DiscordRoleType.SHARE)
                            .ownerId(u.getId())
                            .approverId(null)
                            .created(Instant.now())
                            .build());
                }
            }
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
