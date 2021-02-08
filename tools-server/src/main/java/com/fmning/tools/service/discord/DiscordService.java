package com.fmning.tools.service.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.dto.DiscordObjectDto;
import com.fmning.tools.repository.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fmning.tools.util.DiscordUtil.toHexString;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordService extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;
    private final JDA jda;
    private final ToolsProperties toolsProperties;
    private final ObjectMapper objectMapper;
    private final DiscordRoleRepo discordRoleRepo;

    @Scheduled(cron = "0 0 4 * * *")
    public void announceBirthDay() {
        try {
            discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId()).ifPresent(g -> {
                if (g.isBirthdayEnabled()) {
                    Guild guild = jda.getGuildById(g.getId());
                    MessageChannel channel = guild.getTextChannelById(g.getBirthdayChannelId());

                    Calendar today = Calendar.getInstance();
                    String birthday = String.format("%02d-%02d", today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
                    discordUserRepo.findByBirthday(birthday).forEach(d -> {
                        String message = replacePlaceHolder(g.getBirthdayMessage(), d.getName(), d.getId());
                        channel.sendMessage("@here " + message).queue();

                        try {
                            guild.addRoleToMember(d.getId(), jda.getRoleById(g.getBirthdayRoleId())).queue();
                        } catch (Exception ignored){
                            ignored.printStackTrace();
                        }
                    });

                    Calendar yesterday = Calendar.getInstance();
                    yesterday.add(Calendar.DAY_OF_MONTH, -1);
                    String passedBirthday = String.format("%02d-%02d", yesterday.get(Calendar.MONTH) + 1, yesterday.get(Calendar.DAY_OF_MONTH));
                    discordUserRepo.findByBirthday(passedBirthday).forEach(d -> {
                        try {
                            guild.removeRoleFromMember(d.getId(), jda.getRoleById(g.getBirthdayRoleId())).queue();
                        } catch (Exception ignored){}
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void cleanUpRoleRequests() {
        discordRoleMappingRepo.deleteByCreated(Instant.now().minus(1, ChronoUnit.DAYS));
    }

    public List<DiscordObjectDto> getTextChannels(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            return guild.getTextChannels()
                    .stream()
                    .map(tc -> DiscordObjectDto.builder()
                            .id(tc.getId())
                            .name(tc.getName())
                            .build())
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<DiscordObjectDto> getRoles(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            return guild.getRoles()
                    .stream()
                    .map(r -> DiscordObjectDto.builder()
                            .id(r.getId())
                            .name(r.getName() )
                            .build())
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void seedRoles(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.getRoles().forEach(r -> {
                DiscordRole role = discordRoleRepo.findById(r.getId()).orElse(null);
                if (role == null) {
                    discordRoleRepo.save(DiscordRole.builder()
                            .id(r.getId())
                            .guildId(toolsProperties.getDcDefaultGuildId())
                            .name(r.getName())
                            .color(toHexString(r.getColor()))
                            .position(r.getPositionRaw())
                            .created(Instant.from(r.getTimeCreated())).build());
                }
            });
        }
    }

    public void seedMembers(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.getMembers().forEach(m -> {
                DiscordUser user = discordUserRepo.findById(m.getId()).orElse(null);
                if (user == null) {
                    String roles;
                    try {
                        roles = objectMapper.writeValueAsString(m.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        roles = "";
                    }
                    discordUserRepo.save(DiscordUser.builder()
                            .id(m.getId())
                            .name(m.getUser().getName())
                            .guildId(guildId)
                            .nickname(m.getEffectiveName())
                            .avatarId(m.getUser().getAvatarId())
                            .roles(roles)
                            .createdDate(Instant.from(m.getUser().getTimeCreated()))
                            .joinedDate(Instant.from(m.getTimeJoined()))
                            .boostedDate(m.getTimeBoosted() == null ? null : Instant.from(m.getTimeBoosted()))
                            .build());
                }
            });
        }
    }

}
