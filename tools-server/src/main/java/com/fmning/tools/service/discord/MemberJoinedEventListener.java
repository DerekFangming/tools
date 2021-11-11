package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.fmning.tools.service.discord.AudioPlayerSendHandler.CHINESE_PATTERN;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberJoinedEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            Member member = event.getMember();
            Guild guild = member.getGuild();

            long createdDays = ChronoUnit.DAYS.between(Instant.from(member.getUser().getTimeCreated()), Instant.now());
            if (createdDays < 90) {
                if (!CHINESE_PATTERN.matcher(member.getUser().getName()).find() && member.getUser().getAvatarUrl() == null) {
                    PrivateChannel privateChannel = event.getUser().openPrivateChannel().complete();
                    privateChannel.sendMessage("**哈咯，" + member.getUser().getName() + "，欢迎加入妖风电竞！**\n\n很抱歉你目前不符合加入条件并已被暂时移出我们的频道。" +
                            "为了防止骗子使用大量bot账号加入我们服务器诈骗，妖风电竞对加入账号有如下要求。只要满足`任何一条要求`即可加入服务器。请修改你的账号再重新加入，加入之后" +
                            "你可以取消这些设置，届时我们不会将你移出。我们的永久链接是: https://discord.gg/yaofeng\n\n" +
                            "1. 不可以使用默认头像。请上传一个自定义头像。\n2. 用户名不可以全部是英文。请加入至少一个中文。\n3. 你的账号已创建超过3个月。").complete();

                    discordGuildRepo.findById(event.getGuild().getId()).ifPresent(g -> {
                        if (g.getDebugChannelId() != null) {
                            TextChannel channel = event.getJDA().getTextChannelById(g.getDebugChannelId());
                            if (channel != null) {
                                channel.sendMessage("用户" + member.getUser().getName() + "(" + member.getUser().getId() + ")被移除。头像为" + member.getUser().getAvatarUrl() + "。账号创建天数：" + createdDays).complete();
                            }
                        }
                    });

                    event.getGuild().kick(member).complete();

                    return;
                }
            }

            // Log user join
            discordUserLogRepo.save(DiscordUserLog.builder()
                    .guildId(guild.getId())
                    .userId(member.getId())
                    .name(member.getUser().getName())
                    .nickname(member.getEffectiveName())
                    .action(DiscordUserLogActionType.JOIN)
                    .created(Instant.now())
                    .build());

            // Welcome
            discordGuildRepo.findById(event.getGuild().getId()).ifPresent(g -> {
                if (g.isWelcomeEnabled()) {

                    // Welcome message
                    TextChannel channel = event.getJDA().getTextChannelById(g.getWelcomeChannelId());
                    if (channel != null) {
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle(replacePlaceHolder(g.getWelcomeTitle(), member.getUser().getName(), member.getId()))
                                .setDescription(replacePlaceHolder(g.getWelcomeDescription(), member.getUser().getName(), member.getId()))
                                .setThumbnail(g.getWelcomeThumbnail())
                                .setFooter(g.getWelcomeFooter(), null)
                                .build()).queue();
                    }
                }

                // Role
                if (g.getWelcomeRoleId() != null) {
                    Role role = guild.getRoleById(g.getWelcomeRoleId());
                    if (role != null) {
                        guild.addRoleToMember(member.getId(), role).queue();
                    }
                }
            });


            PrivateChannel privateChannel = event.getUser().openPrivateChannel().complete();
            privateChannel.sendMessage("**哈咯，" + event.getMember().getEffectiveName() + "，欢迎加入妖风电竞！**\n\n我是妖风电" +
                    "竞的专属机器人，独一无二的噢！我最擅长的就是帮你组队啦！在妖风电竞的任意文字频道发送`yf a`就好啦！组队邀请还可以加上你想说的话" +
                    "，查询并显示你当前的战绩并且生成上车链接方便其他玩家点击上车噢。详情可以通过发送`yf help invite`查看！\n\n除此之外，我还会唱歌" +
                    "，祝福生日，吹牛，创建并分享Tag，创建语音频道等等。快去妖风电竞的文字频道使用`yf`命令和我打个招呼吧~").queue();

        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }

    }
}
