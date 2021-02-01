package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.repository.DiscordUserRepo;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberRemoveEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

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
            // delete role // TODO also uboost

            // Remove birthday if registered
            if (discordUser != null) discordUserRepo.delete(discordUser);

        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }
}
