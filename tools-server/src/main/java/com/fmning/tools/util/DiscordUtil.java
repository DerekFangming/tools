package com.fmning.tools.util;

import com.fmning.tools.domain.DiscordCategory;
import com.fmning.tools.domain.DiscordChannel;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordUser;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
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

    public static DiscordRole fromRole(Role role) {
        return DiscordRole.builder()
                .id(role.getId())
                .guildId(role.getGuild().getId())
                .name(role.getName())
                .color(toHexString(role.getColor()))
                .position(role.getPositionRaw())
                .created(Instant.from(role.getTimeCreated()))
                .build();
    }

    public static DiscordCategory fromCategory(Category category) {
        return DiscordCategory.builder()
                .id(category.getId())
                .guildId(category.getGuild().getId())
                .name(category.getName())
                .position(category.getPositionRaw())
                .created(Instant.from(category.getTimeCreated()))
                .build();
    }

    public static DiscordChannel fromChannel(GuildChannel channel) {
        Category category = channel.getParent();
        return DiscordChannel.builder()
                .id(channel.getId())
                .guildId(channel.getGuild().getId())
                .categoryId(category == null ? null : category.getId())
                .name(channel.getName())
                .type(channel.getType())
                .position(channel.getPositionRaw())
                .created(Instant.from(channel.getTimeCreated()))
                .build();
    }

    public static String toHexString(Color color) {
        if (color == null) {
            return null;
        } else {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }
    }
}
