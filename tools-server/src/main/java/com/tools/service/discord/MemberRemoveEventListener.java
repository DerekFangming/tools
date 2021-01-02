package com.tools.service.discord;

import com.tools.domain.DiscordUserLog;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.repository.DiscordUserRepo;
import com.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

            // Log user leave
            discordUserLogRepo.save(DiscordUserLog.builder()
                    .guildId(guild.getId())
                    .userId(user.getId())
                    .name(user.getName())
                    .action(DiscordUserLogActionType.LEAVE)
                    .created(Instant.now())
                    .build());

            // Remove birthday if registered
            discordUserRepo.findById(user.getId()).ifPresent(discordUserRepo::delete);

        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }
}
