package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberJoinedEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            Member member = event.getMember();
            Guild guild = member.getGuild();

            // Log user join
            discordUserLogRepo.save(DiscordUserLog.builder()
                    .guildId(guild.getId())
                    .userId(member.getId())
                    .name(member.getUser().getName())
                    .nickname(member.getEffectiveName())
                    .action(DiscordUserLogActionType.JOIN)
                    .created(Instant.now())
                    .build());

            // Welcome
            discordGuildRepo.findById(event.getGuild().getId()).ifPresent(g -> {
                if (g.isWelcomeEnabled()) {

                    // Welcome message
                    TextChannel channel = event.getJDA().getTextChannelById(g.getWelcomeChannelId());
                    if (channel != null) {
                        channel.sendMessage(new EmbedBuilder()
                                .setTitle(replacePlaceHolder(g.getWelcomeTitle(), member.getUser().getName(), member.getId()))
                                .setDescription(replacePlaceHolder(g.getWelcomeDescription(), member.getUser().getName(), member.getId()))
                                .setThumbnail(g.getWelcomeThumbnail())
                                .setFooter(g.getWelcomeFooter(), null)
                                .build()).queue();
                    }

                    // Role
                    if (g.getWelcomeRoleId() != null) {
                        Role role = guild.getRoleById(g.getWelcomeRoleId());
                        if (role != null) {
                            guild.addRoleToMember(member.getId(), role).queue();
                        }
                    }
                }
            });
        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }

    }
}
