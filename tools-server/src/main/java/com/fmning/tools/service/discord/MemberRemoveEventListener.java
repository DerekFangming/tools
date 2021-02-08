package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.*;
import com.fmning.tools.type.DiscordRoleType;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberRemoveEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        try {
            User user = event.getUser();
            Guild guild = event.getGuild();

            DiscordUser discordUser = discordUserRepo.findById(user.getId()).orElse(null);

            // Log user leave
            discordUserLogRepo.save(DiscordUserLog.builder()
                    .guildId(guild.getId())
                    .userId(user.getId())
                    .name(user.getName())
                    .nickname(discordUser == null ? user.getName() : discordUser.getNickname())
                    .action(DiscordUserLogActionType.LEAVE)
                    .created(Instant.now())
                    .build());

            // Delete role
            discordRoleMappingRepo.findByOwnerId(user.getId()).forEach(rm -> {
                if (rm.getType() == DiscordRoleType.LEVEL || rm.getType() == DiscordRoleType.BOOST) {
                    Role role = guild.getRoleById(rm.getRoleId());
                    if (role != null) role.delete().queue();
                }
                discordRoleMappingRepo.delete(rm);
            });


            // Delete user
            if (discordUser != null) {
                discordUserRepo.delete(discordUser);
            }

        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }
}
