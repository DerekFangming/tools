package com.fmning.tools.util;

import com.fmning.tools.domain.DiscordUser;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

public class DiscordUtil {
    public static DiscordUser fromMember(Member member) {
        return DiscordUser.builder()
                .id(member.getId())
                .guildId(member.getGuild().getId())
                .name(member.getUser().getName())
                .nickname(member.getEffectiveName())
                .avatarId(member.getUser().getAvatarId())
                .createdDate(Instant.from(member.getUser().getTimeCreated()))
                .joinedDate(Instant.from(member.getTimeJoined()))
                .boostedDate(member.getTimeBoosted() == null ? null : Instant.from(member.getTimeBoosted()))
                .build();
    }
}
