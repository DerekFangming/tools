package com.fmning.tools.service.discord;

import com.fmning.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

import static net.dv8tion.jda.api.Permission.ALL_PERMISSIONS;

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
                    .addPermissionOverride(member, ALL_PERMISSIONS, 0)
                    .addPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK, Permission.VOICE_STREAM, Permission.VOICE_USE_VAD,
                                    Permission.PRIORITY_SPEAKER),
                            EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.CREATE_INSTANT_INVITE, Permission.VOICE_CONNECT, Permission.VOICE_MUTE_OTHERS,
                                    Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS))
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
