package com.fmning.tools.service.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberVoiceEventListener extends BaseEventListener  {

    private final DiscordVoiceService discordVoiceService;

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if (!event.getMember().getUser().isBot()) {
            discordVoiceService.memberJoinChannel(event.getChannelJoined(), event.getGuild().getAfkChannel(), event.getMember());
        }
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        if (!event.getMember().getUser().isBot()) {
            discordVoiceService.memberJoinChannel(event.getChannelJoined(), event.getGuild().getAfkChannel(), event.getMember());
        }
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (!event.getMember().getUser().isBot()) {
            discordVoiceService.memberLeaveChannel(event.getMember());
        }
    }

}
