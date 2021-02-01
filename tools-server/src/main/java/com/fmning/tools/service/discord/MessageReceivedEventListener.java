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
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;
    private final AudioPlayerSendHandler audioPlayerSendHandler;
    private final DiscordRoleService discordRoleService;
    private final OkHttpClient client;

    public static Pattern userMentionPattern = Pattern.compile("<@.*?>");
    private Pattern birthdayPattern = Pattern.compile("([0-9][0-9])-([0-3][0-9])");
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
            } else if ("apex".equalsIgnoreCase(command[1])) {
                if (command.length > 3 && "link".equalsIgnoreCase(command[2])) {
                    Request request = new Request.Builder()
                            .url("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + command[3])
                            .addHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a")
                            .build();

                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            if (response.code() != 200) {
                                onFailure(call, new IOException());
                                return;
                            }

                            DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
                            discordUser.setApexId(command[3]);
                            discordUserRepo.save(discordUser);

                            channel.sendMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() + "**").queue();
                        }

                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            channel.sendMessage("<@" + member.getId() + "> 你绑定的Origin ID **" + command[3] +
                                    "** 不存在，请重新绑定。我们的数据来自apex tracker。 你可以尝试在 https://apex.tracker.gg 上搜索你的ID。" +
                                    "你的Origin ID是加好友时输入的ID。如果还是无法找到你的ID，可以给apex tracker提交表格来让他们找到你的账号。" +
                                    "https://thetrackernetwork.com/contact?site=apex.tracker.gg&reason=support").queue();
                        }
                    });
                    return;
                }

                ApexDto apexDto = new ApexDto();

                if (command.length >= 3) {
                    String[] extrasArray = Arrays.copyOfRange(command, 1, command.length);
                    apexDto.setExtras(String.join(" ", extrasArray));
                }

                // Read invitation URL
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    VoiceChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel != null) {
                        Invite invite = voiceChannel.createInvite().complete();
                        apexDto.setInviteUrl(invite.getUrl());
                    }
                }

                DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(null);
                if (discordUser == null) {
                    channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                    return;
                } else if (discordUser.getApexId() == null) {
                    channel.sendMessage(new EmbedBuilder()
                            .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                            .setTitle(apexDto.getExtras())
                            .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                            .setFooter("绑定apex账号之后才能显示战绩。使用yf help查看如何绑定。")
                            .build()).queue();
                    return;
                }

                Request request = new Request.Builder()
                        .url("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + discordUser.getApexId())
                        .addHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a")
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        if (response.code() != 200) {
                            onFailure(call, new IOException());
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());

                            JSONArray segments = json.getJSONObject("data").getJSONArray("segments");
                            for (int i = 0; i < segments.length(); i++) {
                                JSONObject segment = segments.getJSONObject(i);
                                if (segment.getString("type").equals("overview")) {
                                    JSONObject stats = segment.getJSONObject("stats");
                                    if (stats.has("kills")) {
                                        apexDto.setKills(stats.getJSONObject("kills").getString("displayValue"));
                                    } else {
                                        apexDto.setKills("无法读取");
                                    }

                                    JSONObject rankScore = stats.getJSONObject("rankScore");
                                    apexDto.setRankName(rankScore.getJSONObject("metadata").getString("rankName"));
                                    apexDto.setRankAvatar(rankScore.getJSONObject("metadata").getString("iconUrl"));
                                    break;
                                }
                            }
                            channel.sendMessage(new EmbedBuilder()
                                    .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                                    .setThumbnail(apexDto.getRankAvatar())
                                    .setTitle(apexDto.getExtras())
                                    .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                                    .addField("Origin ID", discordUser.getApexId(), true)
                                    .addField("段位", apexDto.getRankName(), true)
                                    .addField("击杀", apexDto.getKills(), true)
                                    .build()).queue();
                        } catch (IOException e) {
                            onFailure(call, e);
                        }
                    }

                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        channel.sendMessage(new EmbedBuilder()
                                .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                                .setTitle(apexDto.getExtras())
                                .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                                .addField("Origin ID", discordUser.getApexId(), true)
                                .addField("段位", "无法读取", true)
                                .addField("击杀", "无法读取", true)
                                .build()).queue();
                    }
                });
            } else if ("birthday".equalsIgnoreCase(command[1])) {
                if (command.length == 2) {
                    List<DiscordUser> users = discordUserRepo.findByBirthdayNotNullOrderByBirthdayAsc();
                    if (users.size() == 0) {
                        channel.sendMessage("**尚未有人注册生日**").queue();
                    } else {
                        channel.sendMessage("**全部已注册的生日**\n\n" + users.stream().map(u -> "**" + u.getBirthday() + ":** " + u.getNickname())
                                .collect(Collectors.joining("\n"))).queue();
                    }
                } else {
                    if (command[2].equalsIgnoreCase("month")) {
                        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                        if (command.length == 4) {
                            try {
                                int givenMonth = Integer.parseInt(command[3]);
                                if (givenMonth > 0 && givenMonth < 13) month = givenMonth;
                            } catch (Exception ignored){}
                        }
                        List<DiscordUser> users = discordUserRepo.findByBirthdayStartingWithOrderByBirthdayAsc(String.format("%02d", month));
                        if (users.size() == 0) {
                            channel.sendMessage("**" + month + "月尚未有人注册生日**").queue();
                        } else {
                            channel.sendMessage("**" + month + "月已注册的生日**\n\n" + users.stream().map(u -> "**" + u.getBirthday() + ":** " + u.getNickname())
                                    .collect(Collectors.joining("\n"))).queue();
                        }
                    } else if (command[2].equalsIgnoreCase("disable")) {
                        discordUserRepo.findById(member.getId()).ifPresent(u -> {
                            u.setBirthday(null);
                            discordUserRepo.save(u);
                            channel.sendMessage("<@" + member.getId() + "> 成功取消生日提醒").queue();
                        });
                    } else {
                        Matcher m = birthdayPattern.matcher(command[2]);
                        if (m.find()) {
                            int month = Integer.parseInt(m.group(1));
                            int day = Integer.parseInt(m.group(2));
                            if (month > 0 && month < 13 && day > 0 && day < 32) {
                                DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
                                discordUser.setBirthday(command[2]);
                                discordUserRepo.save(discordUser);

                                String confirmation = "<@" + member.getId() + "> 成功注册生日为**" + month + "月" + day + "日**。";
                                List<DiscordUser> users = discordUserRepo.findByBirthday(command[2]);
                                String sameDay = users.stream().filter(u -> !u.getId().equals(member.getId())).map(u -> "<@" + u.getId() + ">").collect(Collectors.joining("，"));

                                if (sameDay.length() > 0) {
                                    confirmation += "你和" + sameDay + "同一天生日！";
                                }

                                channel.sendMessage(confirmation).queue();
                                return;
                            }

                        }
                        channel.sendMessage("<@" + member.getId() + "> 无法识别" + command[2] +
                                "。生日格式必须为**月份-日期**， 比如**01-02** 或者 **11-29**").queue();
                    }
                }
            }else if ("nb".equalsIgnoreCase(command[1])) {
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
            } else if ("tag".equalsIgnoreCase(command[1])) {
                discordRoleService.createUpdateRole(command, channel, member, false);
            } else if ("boostTag".equalsIgnoreCase(command[1])) {
                discordRoleService.createUpdateRole(command, channel, member, true);
            } else if ("shareTag".equalsIgnoreCase(command[1])) {
                discordRoleService.shareRole(command, channel, member, event.getMessage().getMentionedMembers());
            } else if ("requestTag".equalsIgnoreCase(command[1])) {
                discordRoleService.requestRole(command, channel, member, event.getMessage().getMentionedMembers());
            } else if ("confirmTag".equalsIgnoreCase(command[1])) {
                discordRoleService.confirmRole(command, channel, member);
            } else if ("deleteTag".equalsIgnoreCase(command[1])) {
                discordRoleService.deleteRole(command, channel, member);
            } else if ("ping".equalsIgnoreCase(command[1])) {
                channel.sendMessage("Bot operational. Latency " + event.getJDA().getGatewayPing() + " ms").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> 无法识别指令 **" + content + "**。请运行yf help查看指令说明。").queue();
            }
        } catch (Exception e) {
            logError(event, discordGuildRepo, e);
        }
    }

    private DiscordUser fromMember(Member member) {
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

    @Data
    @NoArgsConstructor
    static class ApexDto {
        private String extras = null;
        private String kills = "";
        private String rankName = "";
        private String rankAvatar = "";
        private String inviteUrl = null;
    }


}
