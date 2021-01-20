package com.fmning.tools.service.discord;

import com.fmning.tools.repository.DiscordGuildRepo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class BaseEventListener extends ListenerAdapter {

    public String replacePlaceHolder(String text, String username, String Id) {
        if (text == null) return null;
        return text.replaceAll("\\{userName}", username).replaceAll("\\{userMention}", "<@" + Id + ">");
    }

    public void logError(Event event, DiscordGuildRepo discordGuildRepo, Exception e) {
        try {
            JDA jda = event.getJDA();
            String guildId = null;

            if (event instanceof MessageReceivedEvent) {
                guildId = ((MessageReceivedEvent) event).getGuild().getId();
            }

            if (guildId == null) {
                e.printStackTrace();
            } else {
                discordGuildRepo.findById(guildId).ifPresent(g -> {
                    if (g.getDebugChannelId() != null) {

                        Guild guild = jda.getGuildById(g.getId());
                        MessageChannel channel = guild.getTextChannelById(g.getDebugChannelId());
                        channel.sendMessage(new EmbedBuilder()
                                .setTitle(e.getClass().getName() + ": " + e.getMessage())
                                .setDescription("```" + ExceptionUtils.getStackTrace(e) + "```")
                                .build()).queue();
                    }
                });
            }
        } catch (Exception ignored){
            e.printStackTrace();
        }
    }
}
