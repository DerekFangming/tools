package com.fmning.tools.service.discord;

import com.fmning.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordChannelService {

    private final DiscordUserRepo discordUserRepo;

    private String channelId = null;


    public void createChannel(MessageChannel channel, Member member, String channelName) {
        Guild guild = member.getGuild();

//        List<Category> cat = guild.getCategories();
//
//        guild.getCategories().forEach(c -> {
//            System.out.println(c.getId());
//            System.out.println(c.getName());
//        });

        if (channelId == null) {
            Category category = guild.getCategoryById("808924908536856607");
            VoiceChannel vc = category.createVoiceChannel(channelName)
                    .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .complete();
            channelId = vc.getId();
        } else {
            VoiceChannel vc = guild.getVoiceChannelById(channelId);
            vc.getManager().setName(channelName).queue();
        }
    }

    public void deleteChannel(MessageChannel channel, Member member) {
        if (channelId != null) {
            Guild guild = member.getGuild();
            VoiceChannel vc = guild.getVoiceChannelById(channelId);
            vc.delete().queue();
        }
    }

}
