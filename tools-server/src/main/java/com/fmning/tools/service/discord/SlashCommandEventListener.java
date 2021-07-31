package com.fmning.tools.service.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SlashCommandEventListener extends BaseEventListener {

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        System.out.println(event.getName());
//        event.reply("Pong!").queue();
        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(" 请求Apex组队", null, null)
                .setTitle("Some title")
                .setDescription("点击加入房间: )")
                .setFooter("绑定apex账号之后才能显示战绩。使用yf help invite查看如何绑定。")
                .build()).queue();

//        channel.sendMessage(new EmbedBuilder()
//                .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
//                .setTitle(processComment(apexDto.getComments()))
//                .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
//                .setFooter("绑定apex账号之后才能显示战绩。使用yf help invite查看如何绑定。" + (apexDto.getInviteUrl() == null ?
//                        "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : ""))
//                .setColor(shouldEmbedBePink(discordUser) ? pink : null)
//                .build()).complete()

    }
}
