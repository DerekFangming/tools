package com.fmning.tools.util;

import com.fmning.tools.domain.DiscordCategory;
import com.fmning.tools.domain.DiscordChannel;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    public static void sendLongEmbed(MessageChannel channel, Member member, String title, String body) {
        List<String> splitMessage = splitMessage(body);
        if (splitMessage.size() == 1) {
            channel.sendMessage(new EmbedBuilder()
                    .setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
                    .setTitle(title)
                    .setDescription(splitMessage.get(0))
                    .build()).queue();
        } else if (splitMessage.size() > 1) {
            for (int i = 0; i < splitMessage.size(); i ++) {
                if (i == 0) {
                    channel.sendMessage(new EmbedBuilder()
                            .setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
                            .setTitle(title)
                            .setDescription(splitMessage.get(i))
                            .setFooter("Page " + (i + 1) + " of " + splitMessage.size())
                            .build()).complete();
                } else {
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription(splitMessage.get(i))
                            .setFooter("Page " + (i + 1) + " of " + splitMessage.size())
                            .build()).complete();
                }
            }
        }
    }

    public static void sendLongMessage(MessageChannel channel, String message) {
        List<String> splitMessage = splitMessage(message);
        for (String msg : splitMessage) {
            channel.sendMessage(msg).complete();
        }
    }

    private static List<String> splitMessage(String body) {
        List<String> res  = new ArrayList<>();
        String[] lines = body.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            if (sb.length() + line.length() > 2000) {
                res.add(sb.toString());
                sb = new StringBuilder();
            }
            sb.append(line).append("\n");
        }

        if (sb.length() > 0) res.add(sb.toString());
        return res;
    }

}
