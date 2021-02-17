package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordUserRepo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fmning.tools.util.DiscordUtil.fromMember;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordInviteService {

    private final DiscordUserRepo discordUserRepo;
    private final OkHttpClient client;
    private Pattern userPattern = Pattern.compile("<@!(.*?)>");

    public void linkAccount(MessageChannel channel, Member member, String apexId) {
        Request request = new Request.Builder()
                .url("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + apexId)
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
                discordUser.setApexId(apexId);
                discordUserRepo.save(discordUser);

                channel.sendMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() + "**").queue();
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                channel.sendMessage("<@" + member.getId() + "> 你绑定的Origin ID **" + apexId +
                        "** 不存在，请重新绑定。我们的数据来自apex tracker。 你可以尝试在 https://apex.tracker.gg 上搜索你的ID。" +
                        "你的Origin ID是加好友时输入的ID。如果还是无法找到你的ID，可以给apex tracker提交表格来让他们找到你的账号。" +
                        "https://thetrackernetwork.com/contact?site=apex.tracker.gg&reason=support").queue();
            }
        });
    }
    
    public void apexInvite(MessageChannel channel, Member member, String comments) {
        ApexDto apexDto = new ApexDto();
        apexDto.setComments(comments == null ? "Apex" : "Apex " + comments);

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
                    .setTitle(processComment(apexDto.getComments()))
                    .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                    .setFooter("绑定apex账号之后才能显示战绩。使用yf help查看如何绑定。" + (apexDto.getInviteUrl() == null ?
							"在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接" : ""))
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
                            .setTitle(processComment(apexDto.getComments()))
                            .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                            .addField("Origin ID", discordUser.getApexId(), true)
                            .addField("段位", apexDto.getRankName(), true)
                            .addField("击杀", apexDto.getKills(), true)
							.setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接" : "")
                            .build()).queue();
                } catch (IOException e) {
                    onFailure(call, e);
                }
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                channel.sendMessage(new EmbedBuilder()
                        .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                        .setTitle(processComment(apexDto.getComments()))
                        .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "[:race_car: 点此上车 :race_car:](" + apexDto.getInviteUrl() + ")")
                        .addField("Origin ID", discordUser.getApexId(), true)
                        .addField("段位", "无法读取", true)
                        .addField("击杀", "无法读取", true)
						.setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接" : "")
                        .build()).queue();
            }
        });
    }

    public void invite(MessageChannel channel, Member member, String comments) {
        String inviteUrl = null;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null) {
                Invite invite = voiceChannel.createInvite().complete();
                inviteUrl = invite.getUrl();
            }
        }

        channel.sendMessage(new EmbedBuilder()
                .setAuthor(member.getEffectiveName() + " 请求组队", null, member.getUser().getAvatarUrl())
                .setTitle(processComment(comments))
                .setDescription(inviteUrl == null ? inviteUrl : "[:race_car: 点此上车 :race_car:](" + inviteUrl + ")")
                .setThumbnail("https://i.imgur.com/JCIxnvM.jpg")
				.setFooter(inviteUrl == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接" : "")
                .build()).queue();
    }

    private String processComment(String comment) {
        if (comment == null) return comment;
        String res = comment;
        Matcher matcher = userPattern.matcher(comment);
        while (matcher.find()) {
            DiscordUser user = discordUserRepo.findById(matcher.group(1)).orElse(null);
            if (user != null) res = res.replaceAll(matcher.group(0), user.getNickname());
        }

        return res;
    }

    @Data
    @NoArgsConstructor
    static class ApexDto {
        private String comments = null;
        private String kills = "";
        private String rankName = "";
        private String rankAvatar = "";
        private String inviteUrl = null;
    }
}
