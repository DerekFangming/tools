package com.tools.service.discord;

import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberUpdateEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {
//        event.getMember().getTimeBoosted()
        System.out.println("========================================");
        User u = event.getUser();
        Member member = event.getMember();

        System.out.println(member.getNickname());
        System.out.println(u.getAvatarUrl());
        System.out.println(event.toString());

    }


}
