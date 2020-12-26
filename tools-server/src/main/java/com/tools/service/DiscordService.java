package com.tools.service;

import com.tools.domain.DiscordUser;
import com.tools.repository.DiscordUserRepo;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
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
import javax.swing.text.html.Option;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordService {

    private final HttpClient httpClient;
    private final GatewayDiscordClient gateway;
    private final DiscordUserRepo discordUserRepo;

    @PostConstruct
    public void setup() {
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            System.out.println(event.getMessage().getContent());

            Message message = event.getMessage();
            String content = message.getContent();

            // yf apex link donuuttt
            // yf apex 2=1

            if(content.startsWith("yf")) {

                String[] command = content.split(" ");

                MessageChannel channel = message.getChannel().block();
                Member member = event.getMember().get();

                if ("help".equalsIgnoreCase(command[1])) {
                    channel.createMessage("绑定你的Origin ID: yf apex link <Origin Id>\n发送组队邀请: yf apex 2=1").block();
                } else if ("apex".equalsIgnoreCase(command[1]) && "link".equalsIgnoreCase(command[2])) {
                    DiscordUser discordUser = DiscordUser.builder()
                            .id(member.getId().asLong())
                            .name(member.getUsername())
                            .guildId(member.getGuildId().asLong())
                            .apexId(command[3])
                            .build();

                    discordUserRepo.save(discordUser);

                    channel.createMessage(discordUser.getName() + "已绑定Origin ID: " + discordUser.getApexId()).block();
                    return;
                } else if ("apex".equalsIgnoreCase(command[1])) {
                    String extras = "加入频道";
                    if (command.length >= 3) {
                        String[] extrasArray = Arrays.copyOfRange(command, 1, command.length);
                        extras = String.join(" ", extrasArray);
                    }

                    Optional<DiscordUser> discordUserOpt = discordUserRepo.findById(member.getId().asLong());
                    if (!discordUserOpt.isPresent()) {
                        channel.createMessage(member.getUsername() + "未绑定Origin ID. 运行yf help查看如何使用.").block();
                        return;
                    }

                    DiscordUser discordUser = discordUserOpt.get();
                    HttpGet httpGet = new HttpGet("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + discordUser.getApexId());
                    httpGet.setHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a");


//                    String originAvatar = null;
                    String kills = null;
                    String rankName = null;
                    String rankAvatar = null;
                    String inviteUrl = null;

                    try {
                        HttpResponse response = httpClient.execute(httpGet);
                        int status = response.getStatusLine().getStatusCode();
                        if (status == 404) {
                            channel.createMessage("无法找到这个origin ID： " + discordUser.getApexId()).block();
                            return;
                        }
                        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        JSONObject json = new JSONObject(responseBody);

//                        originAvatar = json.getJSONObject("data").getJSONObject("platformInfo").getString("avatarUrl");

                        JSONArray segments = json.getJSONObject("data").getJSONArray("segments");
                        for (int i = 0 ; i < segments.length(); i++) {
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
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }

                    VoiceState voiceState = member.getVoiceState().block();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel().block();
                        if(voiceChannel != null) {
                            ExtendedInvite invite = voiceChannel.createInvite(spec -> spec.setMaxUses(0)).block();
                            inviteUrl = "https://discord.gg/" + invite.getCode();
                        }
                    }

                    String finalRankAvatar = rankAvatar;
                    String finalRankName = rankName;
                    String finalKills = kills;
                    String finalExtras = extras;
                    String finalInviteUrl = inviteUrl;
                    channel.createEmbed(spec ->
                            spec.setAuthor(member.getUsername(),
                                    member.getAvatarUrl(),
                                    member.getAvatarUrl())
                                    .setThumbnail(finalRankAvatar)
                                    .setTitle(finalExtras)
                                    .setDescription(finalInviteUrl)
                                    .addField("Origin", discordUser.getApexId(), true)
                                    .addField("排位", finalRankName, true)
                                    .addField("击杀", finalKills, true)).block();

                }

            }
        });
    }
}
