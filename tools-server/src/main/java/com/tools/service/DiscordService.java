package com.tools.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.domain.DiscordGuild;
import com.tools.domain.DiscordUser;
import com.tools.dto.DiscordObjectDto;
import com.tools.dto.DiscordWelcomeDto;
import com.tools.repository.DiscordGuildRepo;
import com.tools.repository.DiscordUserRepo;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordService {

    private final HttpClient httpClient;
    private final GatewayDiscordClient gateway;
    private final ObjectMapper objectMapper;
    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;

    @PostConstruct
    public void setup() {
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
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
                                            "如果使用这条指令的时候你在妖风电竞的某个语音频道中，组队邀请会自带你当前的语音频道链接，方便其他玩家点击进入。")
                        ).block(Duration.ofSeconds(3));
                    } else if ("apex".equalsIgnoreCase(command[1]) && "link".equalsIgnoreCase(command[2])) {
                        DiscordUser discordUser = DiscordUser.builder()
                                .id(member.getId().asLong())
                                .name(member.getUsername())
                                .guildId(member.getGuildId().asLong())
                                .apexId(command[3])
                                .build();

                        discordUserRepo.save(discordUser);

                        channel.createMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() + "**").block(Duration.ofSeconds(3));
                    } else if ("apex".equalsIgnoreCase(command[1])) {
                        String extras = "加入频道";
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
                                kills = stats.getJSONObject("kills").getString("displayValue");

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
                                        .setDescription(finalInviteUrl)
                                        .addField("Origin", discordUser.getApexId(), true)
                                        .addField("段位", finalRankName, true)
                                        .addField("击杀", finalKills, true)).block(Duration.ofSeconds(3));

                    } else if ("ping".equalsIgnoreCase(command[1])) {
                        channel.createMessage("Bot operational").block(Duration.ofSeconds(3));
                    } else {
                        channel.createMessage("<@" + member.getId().asString() + "> 无法识别指令 **" + content + "**。请运行yf help查看指令说明。").block(Duration.ofSeconds(3));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();// TODO
            }
        });

        gateway.on(MemberLeaveEvent.class).subscribe(event -> {
            Optional<DiscordGuild> gg = discordGuildRepo.findById(event.getGuildId().asLong());
            System.out.println(event.getGuildId().asLong());
        });

        gateway.on(MemberJoinEvent.class).subscribe(event -> {
            System.out.println(event.getGuildId().asLong());
            try {
                discordGuildRepo.findById(event.getGuildId().asLong()).ifPresent(g -> {
                    if (g.isWelcomeEnabled()) {
                        DiscordWelcomeDto dto;
                        try {
                            dto = objectMapper.readValue(g.getWelcomeSetting(), DiscordWelcomeDto.class);
                        } catch (JsonProcessingException e) {
                            throw new IllegalStateException(e);
                        }

                        // Welcome message
                        MessageChannel channel = (MessageChannel) gateway.getChannelById(Snowflake.of(dto.getChannelId())).block(Duration.ofSeconds(3));
                        Member member = event.getMember();
                        channel.createEmbed(spec -> spec
                                .setFooter(dto.getFooter(), null)
                                .setTitle(replacePlaceHolder(dto.getTitle(), member))
                                .setDescription(replacePlaceHolder(dto.getDescription(), member))
                                .setThumbnail(dto.getThumbnail())
                                .setFooter(dto.getFooter(), null)
                        ).block(Duration.ofSeconds(3));

                        // Role
                        if (dto.getRoleId() != 0) {
                            member.addRole(Snowflake.of(dto.getRoleId())).block(Duration.ofSeconds(3));
                        }

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();// TODO
            }
        });
    }

    public List<DiscordObjectDto> getTextChannels(String guildId) {
        return gateway.getGuildChannels(Snowflake.of(guildId))
                .filter(c -> c instanceof TextChannel)
                .map(r -> DiscordObjectDto.builder()
                        .id(r.getId().asLong())
                        .name(r.getName())
                        .build())
                .collectList()
                .block(Duration.ofSeconds(3));
    }

    public List<DiscordObjectDto> getRoles(String guildId) {
        return gateway.getGuildRoles(Snowflake.of(guildId))
                .map(r -> DiscordObjectDto.builder()
                        .id(r.getId().asLong())
                        .name(r.getName())
                        .build())
                .collectList()
                .block(Duration.ofSeconds(3));
    }

    private String replacePlaceHolder(String text, Member member) {
        if (text == null) return null;
        return text.replaceAll("\\{userName}", member.getDisplayName()).replaceAll("\\{userMention}", "<@" + member.getId().asString()+ ">");
    }


}
