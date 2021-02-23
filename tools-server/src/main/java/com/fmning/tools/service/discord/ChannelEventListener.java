package com.fmning.tools.service.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordCategory;
import com.fmning.tools.domain.DiscordChannel;
import com.fmning.tools.repository.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.category.update.GenericCategoryUpdateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.store.update.GenericStoreChannelUpdateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.GenericTextChannelUpdateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.GenericVoiceChannelUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.fmning.tools.util.DiscordUtil.fromCategory;
import static com.fmning.tools.util.DiscordUtil.fromChannel;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ChannelEventListener extends BaseEventListener {
    private final DiscordCategoryRepo discordCategoryRepo;
    private final DiscordChannelRepo discordChannelRepo;


    @Override
    public void onCategoryCreate(@Nonnull CategoryCreateEvent event) {
        DiscordCategory discordCategory = discordCategoryRepo.findById(event.getCategory().getId()).orElse(null);
        if (discordCategory == null) discordCategoryRepo.save(fromCategory(event.getCategory()));
    }

    @Override
    public void onCategoryDelete(@Nonnull CategoryDeleteEvent event) {
        discordCategoryRepo.findById(event.getCategory().getId()).ifPresent(discordCategoryRepo::delete);
    }

    @Override
    public void onGenericCategoryUpdate(@Nonnull GenericCategoryUpdateEvent event) {
        DiscordCategory discordCategory = discordCategoryRepo.findById(event.getCategory().getId()).orElse(fromCategory(event.getCategory()));
        discordCategory.setName(event.getCategory().getName());
        discordCategory.setPosition(event.getCategory().getPositionRaw());

        discordCategoryRepo.save(discordCategory);
    }

    @Override
    public void onStoreChannelCreate(@Nonnull StoreChannelCreateEvent event) {
        createChannel(event.getChannel());
    }

    @Override
    public void onStoreChannelDelete(@Nonnull StoreChannelDeleteEvent event) {
        deleteChannel(event.getChannel());
    }

    @Override
    public void onGenericStoreChannelUpdate(@Nonnull GenericStoreChannelUpdateEvent event) {
        updateChannel(event.getChannel());
    }

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        createChannel(event.getChannel());
    }

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        deleteChannel(event.getChannel());
    }

    @Override
    public void onGenericTextChannelUpdate(@Nonnull GenericTextChannelUpdateEvent event) {
        updateChannel(event.getChannel());
    }

    @Override
    public void onVoiceChannelCreate(@Nonnull VoiceChannelCreateEvent event) {
        createChannel(event.getChannel());
    }

    @Override
    public void onVoiceChannelDelete(@Nonnull VoiceChannelDeleteEvent event) {
        deleteChannel(event.getChannel());
    }

    @Override
    public void onGenericVoiceChannelUpdate(@Nonnull GenericVoiceChannelUpdateEvent event) {
        updateChannel(event.getChannel());
    }

    private void createChannel(GuildChannel channel) {
        DiscordChannel discordChannel = discordChannelRepo.findById(channel.getId()).orElse(null);
        if (discordChannel == null) discordChannelRepo.save(fromChannel(channel));
    }

    private void deleteChannel(GuildChannel channel) {
        discordChannelRepo.findById(channel.getId()).ifPresent(discordChannelRepo::delete);
        // TODO delete MAPPING
    }

    private void updateChannel(GuildChannel channel) {
        DiscordChannel discordChannel = discordChannelRepo.findById(channel.getId()).orElse(fromChannel(channel));
        discordChannel.setName(channel.getName());
        discordChannel.setType(channel.getType());
        discordChannel.setPosition(channel.getPositionRaw());

        discordChannelRepo.save(discordChannel);
    }

}
