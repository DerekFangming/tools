package com.tools.service.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.domain.DiscordUser;
import com.tools.domain.DiscordUserLog;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.repository.DiscordUserRepo;
import com.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberUpdateEventListener extends BaseEventListener {

    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final DiscordGuildRepo discordGuildRepo;
    private final ObjectMapper objectMapper;

    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {
        try {
            User user = event.getUser();
            Member member = event.getMember();

            DiscordUser discordUser = discordUserRepo.findById(user.getId())
                    .orElse(DiscordUser.builder()
                            .id(user.getId())
                            .guildId(member.getGuild().getId())
                            .createdDate(Instant.from(user.getTimeCreated()))
                            .joinedDate(Instant.from(member.getTimeJoined()))
                            .build());


            // Compare
            if (discordUser.getBoostedDate() == null && member.getTimeBoosted() != null) {
                if (Instant.from(member.getTimeBoosted()).isAfter(Instant.now().minus(Duration.ofDays(5)))) {
                    discordUserLogRepo.save(DiscordUserLog.builder()
                            .guildId(discordUser.getGuildId())
                            .userId(discordUser.getId())
                            .name(user.getName())
                            .nickname(member.getEffectiveName())
                            .action(DiscordUserLogActionType.BOOST)
                            .created(Instant.now())
                            .build());
                }
            } else if (discordUser.getBoostedDate() != null && member.getTimeBoosted() == null) {
                discordUserLogRepo.save(DiscordUserLog.builder()
                        .guildId(discordUser.getGuildId())
                        .userId(discordUser.getId())
                        .name(user.getName())
                        .nickname(member.getEffectiveName())
                        .action(DiscordUserLogActionType.UN_BOOST)
                        .created(Instant.now())
                        .build());
            }

            // Update fields that are updatable
            discordUser.setName(user.getName());
            discordUser.setNickname(member.getEffectiveName());
            discordUser.setAvatarId(user.getAvatarId());
            discordUser.setBoostedDate(member.getTimeBoosted() == null ? null : Instant.from(member.getTimeBoosted()));

            List<String> roleIds = member.getRoles().stream().map(Role::getId).collect(Collectors.toList());
            try {
                discordUser.setRoles(objectMapper.writeValueAsString(roleIds));
            } catch (JsonProcessingException ignored) {}

            // Fix existing data
            if (discordUser.getCreatedDate() == null) discordUser.setCreatedDate(Instant.from(user.getTimeCreated()));
            if (discordUser.getJoinedDate() == null) discordUser.setJoinedDate(Instant.from(member.getTimeJoined()));

            // Save
            discordUserRepo.save(discordUser);
        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }

    }


}
