package com.tools.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.domain.DiscordUser;
import com.tools.domain.DiscordUserLog;
import com.tools.dto.DiscordObjectDto;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserLogRepo;
import com.tools.repository.DiscordUserRepo;
import com.tools.type.DiscordUserLogActionType;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordService {

    private final HttpClient httpClient;
    private final GatewayDiscordClient gateway;
    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

    private Pattern userMentionPattern = Pattern.compile("<@.*?>");
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
    }};

    @PostConstruct
    public void setup() {
        gateway.on(MessageCreateEvent.class).onErrorResume(e -> {
            e.printStackTrace();
            return Mono.empty();
        }).subscribe(event -> {
            try {
                Message message = event.getMessage();
                String content = message.getContent();

                if (content.toLowerCase().startsWith("yf")) {

                    String[] command = content.split("\\s+");

                    MessageChannel channel = message.getChannel().block(Duration.ofSeconds(3));
                    Member member = event.getMember().get();

                    if ("help".equalsIgnoreCase(command[1])) {
                        channel.createEmbed(spec ->
                            spec
                                    .setTitle("妖风电竞 bot指令")
                                    .setDescription("**绑定Origin ID：**\n```yf apex link ID```\n将指令里的`ID`替换成你的Origin ID即可。" +
                                            "只需绑定一次，绑定之后，每次使用yf指令组队，都将查询你的战绩。可以多次运行这个指令来修改或者更新你的Discord帐号对应的Origin ID。\n\n" +
                                            "**发送组队邀请：**\n```yf apex 你想说的```\n指令里`你想说的`可以随意输入，比如2=1。它将出现在组队邀请的标题中。" +
                                            "如果使用这条指令的时候你在妖风电竞的某个语音频道中，组队邀请会自带你当前的语音频道链接，方便其他玩家点击进入。\n" +
                                            "**太牛了：**\n```yf nb @某人```\n被@的人太强了！如果要夸自己，可以省略@， 直接使用`yf nb`")
                        ).block(Duration.ofSeconds(3));
                    } else if ("apex".equalsIgnoreCase(command[1])) {
                        if (command.length > 2 && "link".equalsIgnoreCase(command[2])) {
                            DiscordUser discordUser = discordUserRepo.findById(member.getId().asLong()).orElse(DiscordUser.builder().id(member.getId().asLong()).name(member.getUsername()).guildId(member.getGuildId().asLong()).build());
                            discordUser.setApexId(command[3]);
                            discordUserRepo.save(discordUser);

                            channel.createMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() + "**").block(Duration.ofSeconds(3));
                            return;
                        }
                        String extras = "";
                        String kills = "";
                        String rankName = "";
                        String rankAvatar = "";
                        String inviteUrl = "";

                        if (command.length >= 3) {
                            String[] extrasArray = Arrays.copyOfRange(command, 1, command.length);
                            extras = String.join(" ", extrasArray);
                        }

                        Optional<DiscordUser> discordUserOpt = discordUserRepo.findById(member.getId().asLong());
                        if (!discordUserOpt.isPresent()) {
                            channel.createMessage("<@" + member.getId().asString() + "> 你未绑定Origin ID。运行yf help查看如何绑定。").block(Duration.ofSeconds(3));
                            return;
                        }

                        DiscordUser discordUser = discordUserOpt.get();
                        HttpGet httpGet = new HttpGet("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + discordUser.getApexId());
                        httpGet.setHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a");



                        HttpResponse response = httpClient.execute(httpGet);
                        int status = response.getStatusLine().getStatusCode();
                        if (status == 404) {
                            channel.createMessage("<@" + discordUser.getId() + "> 你绑定的Origin ID **" + discordUser.getApexId() +
                                    "** 不存在，请重新绑定。你可以尝试在 https://apex.tracker.gg 上搜索你的ID。你的Origin ID是加好友是输入的" +
                                    "ID，不是登录Origin的用户名。").block(Duration.ofSeconds(3));
                            return;
                        }
                        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        JSONObject json = new JSONObject(responseBody);

                        JSONArray segments = json.getJSONObject("data").getJSONArray("segments");
                        for (int i = 0; i < segments.length(); i++) {
                            JSONObject segment = segments.getJSONObject(i);
                            if (segment.getString("type").equals("overview")) {
                                JSONObject stats = segment.getJSONObject("stats");
                                if (stats.has("kills")) {
                                    kills = stats.getJSONObject("kills").getString("displayValue");
                                } else {
                                    kills = "无法读取";
                                }

                                JSONObject rankScore = stats.getJSONObject("rankScore");
                                rankName = rankScore.getJSONObject("metadata").getString("rankName");
                                rankAvatar = rankScore.getJSONObject("metadata").getString("iconUrl");
                                break;
                            }
                        }

                        VoiceState voiceState = member.getVoiceState().block(Duration.ofSeconds(3));
                        if (voiceState != null) {
                            VoiceChannel voiceChannel = voiceState.getChannel().block(Duration.ofSeconds(3));
                            if (voiceChannel != null) {
                                ExtendedInvite invite = voiceChannel.createInvite(spec -> spec.setMaxUses(0)).block(Duration.ofSeconds(3));
                                inviteUrl = "https://discord.gg/" + invite.getCode();
                            }
                        }

                        String finalRankAvatar = rankAvatar;
                        String finalRankName = rankName;
                        String finalKills = kills;
                        String finalExtras = extras;
                        String finalInviteUrl = inviteUrl;
                        channel.createEmbed(spec ->
                                spec.setAuthor(member.getUsername() + " 请求Apex组队", null, member.getAvatarUrl())
                                        .setThumbnail(finalRankAvatar)
                                        .setTitle(finalExtras)
                                        .setDescription(finalInviteUrl.isEmpty() ? finalInviteUrl : "[:race_car: 点此上车 :race_car:](" + finalInviteUrl + ")")
                                        .addField("Origin ID", discordUser.getApexId(), true)
                                        .addField("段位", finalRankName, true)
                                        .addField("击杀", finalKills, true)).block(Duration.ofSeconds(3));

                    } else if ("birthday".equalsIgnoreCase(command[1])) {
                        if (command.length == 2) {
                            List<DiscordUser> users = discordUserRepo.findByBirthdayNotNull();
                            if (users.size() == 0) {
                                channel.createMessage("**尚未有人注册生日**").block(Duration.ofSeconds(3));
                            } else {
                                channel.createMessage("**全部已注册的生日**\n\n" + users.stream().map(u -> "**" + u.getBirthday() + ":** " + u.getName())
                                        .collect(Collectors.joining("\n"))).block(Duration.ofSeconds(3));
                            }
                        } else {
                            if (command[2].equalsIgnoreCase("month")) {
                                Calendar calendar = Calendar.getInstance();
                                List<DiscordUser> users = discordUserRepo.findByBirthdayStartingWith(String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1));
                                if (users.size() == 0) {
                                    channel.createMessage("**本月尚未有人注册生日**").block(Duration.ofSeconds(3));
                                } else {
                                    channel.createMessage("**本月已注册的生日**\n\n" + users.stream().map(u -> "**" + u.getBirthday() + ":** " + u.getName())
                                            .collect(Collectors.joining("\n"))).block(Duration.ofSeconds(3));
                                }
                            } else if (command[2].equalsIgnoreCase("disable")) {
                                discordUserRepo.findById(member.getId().asLong()).ifPresent(u -> {
                                    u.setBirthday(null);
                                    discordUserRepo.save(u);
                                    channel.createMessage("<@" + member.getId().asString() + "> 成功取消生日提醒").block(Duration.ofSeconds(3));
                                });
                            } else {
                                Matcher m = birthdayPattern.matcher(command[2]);
                                if (m.find()) {
                                    int month = Integer.parseInt(m.group(1));
                                    int day = Integer.parseInt(m.group(2));
                                    if (month > 0 && month < 13 && day > 0 && day < 32) {
                                        DiscordUser discordUser = discordUserRepo.findById(member.getId().asLong()).orElse(DiscordUser.builder().id(member.getId().asLong()).name(member.getUsername()).guildId(member.getGuildId().asLong()).build());
                                        discordUser.setBirthday(command[2]);
                                        discordUserRepo.save(discordUser);
                                        channel.createMessage("<@" + member.getId().asString() + "> 成功注册生日为**" + month +
                                                "月" + day + "日**。" ).block(Duration.ofSeconds(3));
                                        return;
                                    }

                                }
                                channel.createMessage("<@" + member.getId().asString() + "> 无法识别" + command[2] +
                                        "。生日格式必须为**月份-日期**， 比如**01-02** 或者 **11-29**").block(Duration.ofSeconds(3));
                            }
                        }
                    } else if ("ping".equalsIgnoreCase(command[1])) {
                        channel.createMessage("Bot operational").block(Duration.ofSeconds(3));
                    } else if ("nb".equalsIgnoreCase(command[1])) {
                        Matcher matcher = userMentionPattern.matcher(content);
                        String mention = null;
                        while (matcher.find()) {mention= matcher.group(0);}
                        channel.createMessage((mention == null ? ("<@" + member.getId().asString() + ">") : mention) +
                                nbList.get(new Random().nextInt(nbList.size()))).block(Duration.ofSeconds(3));
                    } else if ("yygq".equalsIgnoreCase(command[1])) {
                        channel.createMessage("<@" + member.getId().asString() + "> 警告！ 本DC禁止阴阳怪气！").block(Duration.ofSeconds(3));
                    } else if ("debug".equalsIgnoreCase(command[1])) {
                        channel.createMessage("@here **今天有人生日啦~ 大家一起祝福生日快乐**\n\n\n" +
                                "⊹ ︵︵︵︵︵︵ ・─・ ︵︵︵︵︵︵₊ ๑ ˎˊ˗\n" +
                                "✦┊✧꒰ **HAPPY BIRTHDAY!!** ꒱✦┊✧ \n" +
                                "◦・ ︶︶︶︶︶︶︶︶︶︶︶︶︶︶︶꒷꒦‧₊๑\n\n" +
                                "₊˚๑:tada:꒱✦ 享受你的生日哦, $(usermention) $(tag)\n\n" +
                                "₊˚๑:raised_hands:꒱✧ 你今年又大了一岁哦\n\n" +
                                "₊˚๑:balloon:꒱✦ 妖风电竞在这里祝你生日快乐~\n\n" +
                                "₊˚๑:birthday:꒱✧ 吃一些大餐好好享受你的生日\n\n" +
                                "₊˚๑:gift: ꒱✦ 别忘了还要收到很多礼物哦 :wink:\n\n" +
                                "https://static.tumblr.com/261f1cbb1bcad41152675c8923d961c0/r9aqvat/dxSoepla2/tumblr_static_tumblr_static_314vznl2dpk4s4c0ck4co8cww_focused_v3.jpg")
                                .block(Duration.ofSeconds(3));
                    } else {
                        channel.createMessage("<@" + member.getId().asString() + "> 无法识别指令 **" + content + "**。请运行yf help查看指令说明。").block(Duration.ofSeconds(3));
                    }

                }
            } catch (Exception e) {
                logError(event.getGuildId().orElse(null), e);
            }
        });

        gateway.on(MemberLeaveEvent.class).onErrorResume(e -> {
            e.printStackTrace();
            return Mono.empty();
        }).subscribe(event -> {
            // Log user leave
            event.getMember().ifPresent(m -> discordUserLogRepo.save(DiscordUserLog.builder()
                    .guildId(m.getGuildId().asLong())
                    .userId(m.getId().asLong())
                    .name(m.getUsername())
                    .action(DiscordUserLogActionType.LEAVE)
                    .created(Instant.now())
                    .build())
            );

        });

        gateway.on(MemberJoinEvent.class).onErrorResume(e -> {
            e.printStackTrace();
            return Mono.empty();
        }).subscribe(event -> {
            try {
                Member member = event.getMember();

                // Log user join
                discordUserLogRepo.save(DiscordUserLog.builder()
                        .guildId(member.getGuildId().asLong())
                        .userId(member.getId().asLong())
                        .name(member.getUsername())
                        .action(DiscordUserLogActionType.JOIN)
                        .created(Instant.now())
                        .build());

                // Welcome
                discordGuildRepo.findById(event.getGuildId().asString()).ifPresent(g -> {
                    if (g.isWelcomeEnabled()) {

                        // Welcome message
                        MessageChannel channel = (MessageChannel) gateway.getChannelById(Snowflake.of(g.getWelcomeChannelId())).block(Duration.ofSeconds(3));
                        channel.createEmbed(spec -> spec
                                .setFooter(g.getWelcomeFooter(), null)
                                .setTitle(replacePlaceHolder(g.getWelcomeTitle(), member))
                                .setDescription(replacePlaceHolder(g.getWelcomeDescription(), member))
                                .setThumbnail(g.getWelcomeThumbnail())
                        ).block(Duration.ofSeconds(3));

                        // Role
                        if (g.getWelcomeRoleId() != null) {
                            member.addRole(Snowflake.of(g.getWelcomeRoleId())).block(Duration.ofSeconds(3));
                        }

                    }
                });
            } catch (Exception e) {
                logError(event.getGuildId(), e);
            }
        });
    }

    public List<DiscordObjectDto> getTextChannels(String guildId) {
        return gateway.getGuildChannels(Snowflake.of(guildId))
                .filter(c -> c instanceof TextChannel)
                .map(r -> DiscordObjectDto.builder()
                        .id(r.getId().asString())
                        .name(r.getName())
                        .build())
                .collectList()
                .block(Duration.ofSeconds(3));
    }

    public List<DiscordObjectDto> getRoles(String guildId) {
        return gateway.getGuildRoles(Snowflake.of(guildId))
                .map(r -> DiscordObjectDto.builder()
                        .id(r.getId().asString())
                        .name(r.getName())
                        .build())
                .collectList()
                .block(Duration.ofSeconds(3));
    }

    private String replacePlaceHolder(String text, Member member) {
        if (text == null) return null;
        return text.replaceAll("\\{userName}", member.getUsername()).replaceAll("\\{userMention}", "<@" + member.getId().asString()+ ">");
    }

    private void logError(Snowflake guildId, Exception e) {
        try {
            if (guildId == null) {
                e.printStackTrace();
            } else {
                discordGuildRepo.findById(guildId.asString()).ifPresent(g -> {
                    if (g.getDebugChannelId() != null) {

                        // Welcome message
                        MessageChannel channel = (MessageChannel) gateway.getChannelById(Snowflake.of(g.getDebugChannelId())).block(Duration.ofSeconds(3));
                        channel.createEmbed(spec -> spec
                                .setTitle(e.getClass().getName() + ": " + e.getMessage())
                                .setDescription("```" + ExceptionUtils.getStackTrace(e) + "```")
                        ).block(Duration.ofSeconds(3));

                    }
                });
            }
        } catch (Exception ignored){
            e.printStackTrace();
        }
    }


}
