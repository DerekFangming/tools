package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordTask;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordTaskRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.type.DiscordTaskType;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MemberJoinedEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final DiscordTaskRepo discordTaskRepo;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            Member member = event.getMember();
            Guild guild = member.getGuild();

            long createdDays = ChronoUnit.DAYS.between(Instant.from(member.getUser().getTimeCreated()), Instant.now());
            if (createdDays < 30) {

                DiscordTask task = discordTaskRepo.findByTypeAndPayloadContaining(DiscordTaskType.JOIN_CODE, member.getId());
                String name = "妖风临时-" + RandomStringUtils.randomAlphanumeric(6);
                if (task == null) {
                    discordTaskRepo.save(DiscordTask.builder()
                            .guildId(member.getGuild().getId())
                            .type(DiscordTaskType.JOIN_CODE)
                            .payload(member.getId() + ":" + name)
                            .timeout(Instant.now().plusSeconds(600))
                            .created(Instant.now())
                            .build());

                    kickInvalidUser(event, createdDays, name);
                    return;
                } else {
                    name = task.getPayload().split(":")[1];
                    if (!event.getUser().getName().equals(name)) {
                        kickInvalidUser(event, createdDays, name);
                        return;
                    }
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

                // Warning
                if (createdDays <= 90 && g.getDebugChannelId() != null) {
                    TextChannel channel = event.getJDA().getTextChannelById(g.getDebugChannelId());
                    if (channel != null) {
                        channel.sendMessage("已允许用户" + event.getUser().getName() + "(" + event.getUser().getId() + ")加入。头像为" +
                                event.getUser().getAvatarUrl() + " 。账号创建天数：" + createdDays).complete();
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

    private void kickInvalidUser(GuildMemberJoinEvent event, long createdDays, String name) {
        PrivateChannel privateChannel = event.getUser().openPrivateChannel().complete();
        privateChannel.sendMessage("**哈咯，" + event.getUser().getName() + "，欢迎加入妖风电竞！**\n\n很抱歉你目前不符合加入条件并已被暂时移出我们的频道。" +
                "为了防止骗子使用大量bot账号加入我们服务器进行诈骗，新创建的账号需要通过验证才可以加入妖风电竞。请将你的用户名改成下面的名字之后再重新加入，加入频道之后" +
                "你可以重新改成原来的名字，届时我们不会将你移出频道。这个名字的有效期为10分钟，过期之后再加入时，会生成新的名字。名字修改完成后可以点击这里重新加入我们的频道" +
                ": https://discord.gg/yaofeng\n\n**" + name + "**").complete();

        discordGuildRepo.findById(event.getGuild().getId()).ifPresent(g -> {
            if (g.getDebugChannelId() != null) {
                TextChannel channel = event.getJDA().getTextChannelById(g.getDebugChannelId());
                if (channel != null) {
                    channel.sendMessage("用户" + event.getUser().getName() + "(" + event.getUser().getId() + ")被移除。头像为" +
                            event.getUser().getAvatarUrl() + " 。账号创建天数：" + createdDays + " 。要求改名为：" + name).complete();
                }
            }
        });

        event.getGuild().kick(event.getMember()).complete();
    }
}
