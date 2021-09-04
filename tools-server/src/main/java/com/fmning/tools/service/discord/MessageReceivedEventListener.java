package com.fmning.tools.service.discord;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MessageReceivedEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final DiscordRoleService discordRoleService;
    private final DiscordInviteService discordInviteService;
    private final DiscordBirthdayService discordBirthdayService;
    private final DiscordMusicService discordMusicService;
    private final DiscordMiscService discordMiscService;
    private final DiscordChannelService discordChannelService;
    private final DiscordVoiceService discordVoiceService;
    private final ToolsProperties toolsProperties;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            MessageChannel channel = event.getChannel();
            Member member = event.getMember();
            if (channel.getType() == ChannelType.PRIVATE) {
                if (!event.getMessage().getAuthor().isBot()) {
                    channel.sendMessage("请到妖风电竞的文字频道和我互动噢！").queue();
                }
                return;
            }

            String content = event.getMessage().getContentRaw();
            if (!content.toLowerCase().startsWith("yf")) {
                if (event.getMessage().getType() == MessageType.GUILD_MEMBER_BOOST) {
                    if (member != null) {
                        discordUserLogRepo.save(DiscordUserLog.builder()
                                .guildId(event.getGuild().getId())
                                .userId(member.getUser().getId())
                                .name(member.getUser().getName())
                                .nickname(member.getEffectiveName())
                                .action(DiscordUserLogActionType.BOOST)
                                .created(Instant.now())
                                .build());
                    }
                }
                discordMiscService.checkViolations(channel, member, event.getMessage(), content);
                return;
            }

            Command command = new Command(content);

            if (member == null || member.getUser().isBot()) return;

            if (channel.getId().equals(toolsProperties.getApexChannelId())) {
                if (!command.equals(1, "apex", "a") || command.equals(2, "link", "l")) {
                    channel.sendMessage("<@" + member.getId() + "> 本频道只能使用yf组队命令。请到<#" + toolsProperties.getSelfServiceBotChannelId()
                            + "> 频道使用其他bot命令。").queue();
                    return;
                }
            }

            if (command.length() == 1 || command.equals(1, "help", "h")) {
                if (command.equals(2, "invite", "i")) {
                    discordMiscService.helpInvite(channel);
                } else if (command.equals(2, "tag", "t")) {
                    discordMiscService.helpTag(channel);
                } else if (command.equals(2, "channel", "c")) {
                    discordMiscService.helpChannel(channel);
                } else {
                    discordMiscService.help(channel);
                }
            } else if (command.equals(1, "apex", "a")) {
                if (command.length() > 3 && command.equals(2, "link", "l")) {
                    discordInviteService.linkAccount(channel, member, command.from(3));
                } else if (command.length() == 4 && command.equals(2, "id", "i") && event.getMessage().getMentionedMembers().size() > 0) {
                    discordInviteService.showApexId(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else {
                    discordInviteService.apexInvite(channel, event.getMessage(), member, command.from(2));
                }
            }  else if (command.equals(1, "invite", "i")) {
                discordInviteService.invite(channel, member, command.from(2));
            } else if (command.equals(1, "birthday", "b")) {
                if (command.length() == 2) {
                    discordBirthdayService.listAll(channel);
                } else if (command.equals(2, "month", "m")) {
                    discordBirthdayService.listMonth(channel, command.get(3));
                } else if (command.equals(2, "delete", "d")) {
                    discordBirthdayService.disable(channel, member);
                } else if (event.getMessage().getMentionedMembers().size() > 0) {
                    discordBirthdayService.getBirthday(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else {
                    discordBirthdayService.register(channel, member, command.from(2));
                }
            } else if (command.equals(1, "nb", "n")) {
                discordMiscService.nb(channel, member, event.getMessage().getMentionedMembers());
            } else if (command.equals(1, "come", null)) {
                discordMusicService.join(member, event.getGuild().getAudioManager());
            } else if (command.equals(1, "play", "p")) {
                discordMusicService.play(channel, member, event.getGuild().getAudioManager(), command.from(2));
            } else if (command.equals(1, "skip", null)) {
                discordMusicService.skip();
            } else if (command.equals(1, "stop", null)) {
                discordMusicService.stop();
            } else if (command.equals(1, "queue", "q")) {
                discordMusicService.showQueue(channel);
            } else if (command.equals(1, "loop", null)) {
                discordMusicService.toggleLoop(channel, member);
            } else if (command.equals(1, "tag", "t")) {
                if (command.length() == 2) {
                    discordRoleService.getRoleStatus(channel, member);
                } else if (command.length() == 3 && command.equals(2, "delete", "d")) {
                    discordRoleService.showDeleteStatue(channel, member);
                } else if (command.length() == 4 && command.equals(2, "delete", "d")) {
                    discordRoleService.deleteRole(channel, member, command.get(3));
                } else if (command.length() == 4 && command.equals(2, "share", "s") && event.getMessage().getMentionedMembers().size() > 0) {
                    discordRoleService.shareRole(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else if (command.length() == 4 && command.equals(2, "request", "r") && event.getMessage().getMentionedMembers().size() > 0) {
                    discordRoleService.requestRole(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else if (command.length() == 4 && command.equals(2, "confirm", "c")) {
                    discordRoleService.confirmRole(channel, member, command.get(3));
                } else if (command.length() >= 4) {
                    if (command.length() >= 5 && command.equals(2, "boost", "b")) {
                        discordRoleService.createUpdateRole(channel, member, command.get(3), command.from(4), true);
                    } else {
                        discordRoleService.createUpdateRole(channel, member, command.get(2), command.from(3), false);
                    }
                } else {
                    invalidCommand(channel, member, content);
                }
            } else if (command.equals(1, "channel", "c")) {
                if (command.length() == 2) {
                    discordChannelService.getChannelStatus(channel, member);
                } else if (command.length() == 3 && command.equals(2, "delete", "d")) {
                    discordChannelService.deleteChannel(channel, member, false);
                } else if (command.length() == 4 && command.equals(2, "boost", "b") && command.equals(3, "delete", "d")) {
                    discordChannelService.deleteChannel(channel, member, true);
                } else if (command.length() >= 4 && command.equals(2, "boost", "b")) {
                    discordChannelService.createChannel(channel, member, command.from(3), true);
                } else if (command.length() >= 3) {
                    discordChannelService.createChannel(channel, member, command.from(2), false);
                } else {
                    invalidCommand(channel, member, content);
                }
            } else if (command.equals(1, "lottery", "l")) {
                discordVoiceService.getLotteryStatus(channel, member);
            } else if (command.equals(1, "ping", null)) {
                channel.sendMessage("Bot operational. Latency " + event.getJDA().getGatewayPing() + " ms").queue();
            } else if (command.equals(1, "debug", "d")) {

                discordMiscService.getStatus(channel,member);
//                BufferedImage image = ImageIO.read(new File("D:/share/456.png"));
//
//                Graphics g = image.getGraphics();
//                g.setFont(g.getFont().deriveFont(30f));
//                g.drawString("Hello World!", 100, 100);
//                g.dispose();
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(image, "png", baos);

//                String fonts[] =
//                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//
//                for ( int i = 0; i < fonts.length; i++ )
//                {
//                    System.out.println(fonts[i]);
//                }

//                Font tr = new Font("TimesRoman", Font.PLAIN, 18);
//                Font trb = new Font("TimesRoman", Font.BOLD, 18);
//                Font tri = new Font("TimesRoman", Font.ITALIC, 18);
//                Font trbi = new Font("TimesRoman", Font.BOLD+Font.ITALIC, 18);
//                Font h = new Font("Helvetica", Font.PLAIN, 18);
//                Font c = new Font("Courier", Font.PLAIN, 18);
//                Font d = new Font("BradleyHandITC", Font.PLAIN, 18);
//                Font z = new Font("Bradley Hand ITC", Font.PLAIN, 18);
//
//                BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g = img.createGraphics();
//                g.setFont(tr);
//                g.drawString("123456789 (times roman plain)",10,25);
//                g.setFont(trb);
//                g.drawString("123456789 (times roman bold)",10,50);
//                g.setFont(tri);
//                g.drawString("123456789 (times roman italic)",10,75);
//                g.setFont(trbi);
//                g.drawString("123456789 (times roman bold & italic)",10,100);
//                g.setFont(h);
//                g.drawString("123456789 (helvetica)",10,125);
//                g.setFont(c);
//                g.drawString("123456789 (courier)",10,150);
//                g.setFont(d);
//                g.drawString("123456789 (dialog)",10,175);
//                g.setFont(z);
//                g.drawString("123456789 (zapf dingbats)",10,200);



//                channel.sendMessageEmbeds(new EmbedBuilder()
//                        .setAuthor(member.getEffectiveName() + " 请求组队", null, member.getUser().getAvatarUrl())
//                        .setTitle("Some title")
//                        .setDescription("Description something")
//                        .setThumbnail("attachment://cat.png")
//                        .addField("rank", "hahahaha", true)
////                        .setImage("attachment://cat.png")
//                        .setFooter("在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。")
//                        .build()).addFile(baos.toByteArray(), "cat.png").complete();
//
//                // ROUND IMG "https://www.pngitem.com/pimgs/m/117-1175400_1-to-10-numbers-png-number-10-in.png"
//                // WIDE IMG "https://www.robin-noorda.com/uploads/1/6/8/3/16830688/3347022_orig.jpg"
            } else {
                invalidCommand(channel, member, content);
            }
        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if ("❌".equals(event.getReactionEmote().getName()) && discordInviteService.isCancelable(event.getMessageId(), event.getUserId())) {
            discordInviteService.removeMessageId(event.getMessageId());
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
        } else if ("\uD83D\uDC4D".equals(event.getReactionEmote().getName()) && !event.getUser().isBot()) {
            // TODO: remove when speed is done
            if (event.getMessageId().equals(DiscordService.speedMessageId)) {
                Member member = event.getMember();
                Role role = event.getGuild().getRoleById(DiscordService.roleId);
                if (role != null) {
                    event.getGuild().addRoleToMember(member, role).queue();
                }
            }
        }
    }

    private void invalidCommand(MessageChannel channel, Member member, String content) {
        channel.sendMessage("<@" + member.getId() + "> 无法识别指令 **" + content + "**。请运行yf help查看指令说明。").queue();
    }

    public class Fonts extends Frame
    {
        public void paint(Graphics g)
        {
            setBackground(Color.black);
            setForeground(Color.white);

        }
    }
}
