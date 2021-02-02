package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordGuild;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.domain.DiscordUserLog;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserLogRepo;
import com.fmning.tools.repository.DiscordUserRepo;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class MessageReceivedEventListener extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final AudioPlayerSendHandler audioPlayerSendHandler;
    private final DiscordRoleService discordRoleService;
    private final DiscordInviteService discordInviteService;
    private final DiscordBirthdayService discordBirthdayService;

    public static Pattern userMentionPattern = Pattern.compile("<@.*?>");
    private ArrayList<String> nbList = new ArrayList<String>() {{
        add(" 可太牛逼了");
        add(" 真是帅炸了");
        add(" tql tql tql");
        add(" 带带我带带我");
        add(" 真会玩");
        add(" 沃日这波无敌啊");
        add(" 大腿带带我");
        add(" 还有这种操作，学到了学到了");
        add(" 6666666666 很骚");
        add(" 哇 好帅啊");
        add(" 大佬 tql");
        add(" 超神啦超神啦");
        add(" 有内味儿了");
        add(" 有点东西");
        add(" 对面就这？就这？");
        add(" 美汁汁儿啊！");
    }};

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            String content = event.getMessage().getContentRaw();
            if (!content.toLowerCase().startsWith("yf")) {
                if (event.getMessage().getType() == MessageType.GUILD_MEMBER_BOOST) {
                    Member member = event.getMember();
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
                return;
            }

            Command command1 = new Command(content); // TODO

            String[] command = content.split("\\s+");

            MessageChannel channel = event.getChannel();
            Member member = event.getMember();
            if (member == null || member.getUser().isBot()) return;

            if (command.length == 1 || "help".equalsIgnoreCase(command[1])) {
                channel.sendMessage(new EmbedBuilder()
                        .setTitle("妖风电竞 bot指令")
                        .setDescription("**绑定Origin ID：**`yf apex link ID`\n将指令里的`ID`替换成你的Origin ID即可。" +
                                "只需绑定一次，绑定之后，每次使用yf指令组队，都将查询你的战绩。可以多次运行这个指令来修改或者更新你的Discord帐号对应的Origin ID。\n\n" +
                                "**发送组队邀请：**`yf apex 你想说的`\n指令里`你想说的`可以随意输入，比如2=1。它将出现在组队邀请的标题中。" +
                                "如果使用这条指令的时候你在妖风电竞的某个语音频道中，组队邀请会自带你当前的语音频道链接，方便其他玩家点击进入。\n\n" +
                                "**太牛了：**`yf nb @某人`\n被@的人太强了！如果要夸自己，可以省略@， 直接使用`yf nb`\n\n" +
                                "**注册生日：**`yf birthday MM-DD`\n注册你的生日。注册后生日当天会在生日频道得到祝福以及专属Tag。\n\n" +
                                "**取消生日提醒：**`yf birthday disable`\n\n" +
                                "**全部注册的生日：**`yf birthday`\n\n" +
                                "**本月过生日的成员：**`yf birthday month`\n\n" +
                                "**唱歌：**`yf play 关键字或者youtube歌曲链接`\n把歌曲加入播放队列。 如果当前队列中无歌曲，直接开始播放。\n\n" +
                                "**循环当前歌曲：**`yf loop`\n循环播放正在播放的歌曲。再次运行这个指令或者使用`yf skip`取消循环。\n\n" +
                                "**显示当前播放队列：**`yf queue`\n\n" +
                                "**跳过当前正在播放的歌曲：**`yf skip`\n\n" +
                                "**停止播放并清空播放队列：**`yf stop`")
                        .build()).queue();
            } else if (command1.equals(1, "apex", "a")) {
                if (command1.length() > 3 && command1.equals(2, "link", "l")) {
                    discordInviteService.linkAccount(channel, member, command1.from(3));
                } else {
                    discordInviteService.apexInvite(channel, member, command1.from(2));
                }
            }  else if (command1.equals(1, "invite", "i")) {
                discordInviteService.invite(channel, member, command1.from(2));
            } else if (command1.equals(1, "birthday", "b")) {
                if (command1.length() == 2) {
                    discordBirthdayService.listAll(channel);
                } else if (command1.equals(2, "month", "m")) {
                    discordBirthdayService.listMonth(channel, command1.get(3));
                } else if (command1.equals(2, "disable", "d")) {
                    discordBirthdayService.disable(channel, member);
                } else {
                    discordBirthdayService.register(channel, member, command1.from(2));
                }
            } else if ("nb".equalsIgnoreCase(command[1])) {
                Matcher matcher = userMentionPattern.matcher(content);
                String mention = null;
                while (matcher.find()) {mention= matcher.group(0);}
                channel.sendMessage((mention == null ? ("<@" + member.getId() + ">") : mention) +
                        nbList.get(new Random().nextInt(nbList.size()))).queue();
            } else if ("come".equalsIgnoreCase(command[1])) {
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    VoiceChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel != null) {
                        AudioManager audioManager = event.getGuild().getAudioManager();
                        audioManager.setSendingHandler(audioPlayerSendHandler);
                        audioManager.openAudioConnection(voiceChannel);
                    }
                }
            } else if ("play".equalsIgnoreCase(command[1])) {

                VoiceChannel voiceChannel = null;
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    voiceChannel = voiceState.getChannel();
                }
                if (voiceChannel == null) {
                    channel.sendMessage("<@" + member.getId() + "> 你必须加入一个语音频道才能使用此指令。").queue();
                    return;
                }
                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.setSendingHandler(audioPlayerSendHandler);
                audioManager.openAudioConnection(voiceChannel);

                String[] extrasArray = Arrays.copyOfRange(command, 2, command.length);

                audioPlayerSendHandler.loadAndPlay(String.join(" ", extrasArray), channel, member.getId());


            } else if ("skip".equalsIgnoreCase(command[1])) {
                audioPlayerSendHandler.skip();
            } else if ("stop".equalsIgnoreCase(command[1])) {
                audioPlayerSendHandler.stop();
            } else if ("queue".equalsIgnoreCase(command[1])) {
                audioPlayerSendHandler.showQueue(channel);
            } else if ("loop".equalsIgnoreCase(command[1])) {
                audioPlayerSendHandler.toggleLoop(channel, member.getId());
            } else if (command1.equals(1, "tag", "t")) {
                if (command1.length() == 2) {
                    discordRoleService.getRoleStatus(channel, member);
                } else if (command1.length() == 3 && command1.equals(2, "delete", "d")) {
                    discordRoleService.deleteRole(channel, member);
                } else if (command1.length() == 4 && command1.equals(2, "share", "s") && event.getMessage().getMentionedMembers().size() > 0) {
                    discordRoleService.shareRole(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else if (command1.length() == 4 && command1.equals(2, "request", "r") && event.getMessage().getMentionedMembers().size() > 0) {
                    discordRoleService.requestRole(channel, member, event.getMessage().getMentionedMembers().get(0));
                } else if (command1.length() == 4 && command1.equals(2, "confirm", "c")) {
                    discordRoleService.confirmRole(channel, member, command1.get(3));
                } else if (command1.length() == 4) {
                    discordRoleService.createUpdateRole(channel, member, command1.get(2), command1.get(3),  false);
                } else if (command1.length() == 5 && command1.equals(2, "boost", "b")) {
                    discordRoleService.createUpdateRole(channel, member, command1.get(3), command1.get(4), true);
                } else {
                    invalidCommand(channel, member, content);
                }
            } else if ("ping".equalsIgnoreCase(command[1])) {
                channel.sendMessage("Bot operational. Latency " + event.getJDA().getGatewayPing() + " ms").queue();
            } else {
                invalidCommand(channel, member, content);
            }
        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }

    private void invalidCommand(MessageChannel channel, Member member, String content) {
        channel.sendMessage("<@" + member.getId() + "> 无法识别指令 **" + content + "**。请运行yf help查看指令说明。").queue();
    }


}
