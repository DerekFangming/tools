package com.fmning.tools.service.discord;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordUserRepo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import okhttp3.*;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fmning.tools.service.discord.DiscordMiscService.*;
import static com.fmning.tools.util.DiscordUtil.fromMember;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordInviteService {

    private final DiscordMiscService discordMiscService;
    private final DiscordUserRepo discordUserRepo;
    private final ToolsProperties toolsProperties;
    private final OkHttpClient client;
    private Pattern userPattern = Pattern.compile("<@!(.*?)>");
    private String pinkRoleId = "765439046922272808";
    private Color pink = Color.decode("#e48aa7");

    PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<String, String> expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(1, TimeUnit.HOURS);
    PassiveExpiringMap<String, String> messageOwnership = new PassiveExpiringMap<>(expirationPolicy, new HashMap<>());

    public void linkAccount(MessageChannel channel, Member member, String apexId) {
        Request request = new Request.Builder()
                .url("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + apexId)
                .addHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() == 200) {
                    DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
                    discordUser.setApexId(apexId);
                    discordUserRepo.save(discordUser);

                    channel.sendMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() + "**").queue();
                } else if (response.code() == 500) {
                    DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
                    discordUser.setApexId(apexId);
                    discordUserRepo.save(discordUser);

                    channel.sendMessage("<@" + discordUser.getId() + "> 你已绑定Origin ID: **" + discordUser.getApexId() +
                            "**。但是apex tracker发生了系统问题，暂时无法读取你的战绩。你现在可以正常使用组队命令，但是只有待apex tracker解决他们的问题之后，你的组队命令才能显示战绩。").queue();
                } else {
                    channel.sendMessage("<@" + member.getId() + "> 你绑定的Origin ID **" + apexId + "** 不存在，请重新绑定。我们的数据来自apex tracker网站，" +
                                    "你可以尝试在 https://apex.tracker.gg/ 上搜索你的Origin ID。\n\n •  该网站目前不支持steam账号数据。如果你是用steam玩apex，" +
                                    "请使用你的steam账号绑定的Origin ID。\n •  如果你刚刚改了用户名或者无法搜索到steam绑定的origin账号，请尝试登陆一次origin客户端，" +
                                    "然后再来绑定ID。\n •  如果还是无法找到你的ID，可以联系apex tracker客服，通过提交表格来让他们找到你的账号。" +
                                    "https://thetrackernetwork.com/contact?site=apex.tracker.gg&reason=support").queue(m -> m.suppressEmbeds(true).queue());
                }
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                channel.sendMessage("<@" + member.getId() + "> apex tracker发生了系统问题，暂时无法读取你的战绩。你现在可以正常使用组队命令。请稍后再尝试重新绑定。").queue();
            }
        });
    }

    public void showApexId(MessageChannel channel, Member member, Member mentionedMember) {
        DiscordUser user = discordUserRepo.findById(mentionedMember.getId()).orElse(null);

        if (user == null || user.getApexId() == null) {
            channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "尚未绑定Apex Id。").queue();
        } else {
            channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "绑定的Apex Id为 **" + user.getApexId() + "**").queue();
        }
    }
    
    public void apexInvite(MessageChannel channel, Message message, Member member, String comments) {
        ApexDto apexDto = new ApexDto();
        apexDto.setComments(comments == null ? "Apex" : "Apex " + comments);

        // Read invitation URL
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null) {
                Invite invite = voiceChannel.createInvite().complete();
                apexDto.setInviteUrl(invite.getUrl());
                apexDto.setChannelName(voiceChannel.getName());
            }
        }

        DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(null);
        if (discordUser == null) {
            channel.sendMessage("<@" + member.getId() + "> 系统错误，请稍后再试。").queue();
            return;
        } else if (apexDto.getInviteUrl() == null && toolsProperties.getTeamLimitChannelId().contains(channel.getId())) {
            discordMiscService.warnUser(message, member, discordUser, APEX_WARNING_TITLE,
                    String.format(APEX_WARNING_NOT_IN_CHANNEL, toolsProperties.getSelfServiceBotChannelId()));
            return;
        } else if (apexDto.getComments().contains("私")) {
            discordMiscService.warnUser(message, member, discordUser, APEX_WARNING_TITLE,
                    String.format(APEX_WARNING_BODY, message.getContentRaw(), toolsProperties.getSelfServiceBotChannelId()));
            return;
        } else if (discordUser.getApexId() == null) {
            makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                    .setTitle(processComment(apexDto.getComments()))
                    .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
                    .setFooter("绑定apex账号之后才能显示战绩。在 \uD83D\uDCAB自助bot 频道使用yf help invite查看如何绑定。" + (apexDto.getInviteUrl() == null ?
							"在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : ""))
                    .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                    .build()).complete(), member);
            return;
        }

        Request request = new Request.Builder()
                .url("https://public-api.tracker.gg/v2/apex/standard/profile/origin/" + discordUser.getApexId())
                .addHeader("TRN-Api-Key", "0721ec03-b743-40ff-97fa-0d04568f655a")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() == 200) {
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
                                if (stats.has("damage")) {
                                    apexDto.setDamage(stats.getJSONObject("damage").getString("displayValue"));
                                } else {
                                    apexDto.setDamage("无法读取");
                                }

                                if (stats.has("arenaRankScore")) {
                                    apexDto.setArenaRank(stats.getJSONObject("arenaRankScore").getJSONObject("metadata").getString("rankName"));
                                } else {
                                    apexDto.setArenaRank("无法读取");
                                }

                                JSONObject rankScore = stats.getJSONObject("rankScore");
                                apexDto.setRankName(rankScore.getJSONObject("metadata").getString("rankName"));
                                apexDto.setRankAvatar(rankScore.getJSONObject("metadata").getString("iconUrl"));
                                try {
                                    apexDto.setRankPlacement(Integer.toString(rankScore.getInt("rank")));
                                } catch (Exception ignored){
                                    apexDto.setRankPlacement("未上榜");
                                }
                                break;
                            }
                        }

                        makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                                .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                                .setThumbnail(apexDto.getRankAvatar())
                                .setTitle(processComment(apexDto.getComments()))
                                .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
                                .addField("Origin ID", discordUser.getApexId(), true)
                                .addField("总击杀", apexDto.getKills(), true)
                                .addField("总伤害", apexDto.getDamage(), true)
                                .addField("段位", apexDto.getRankName(), true)
                                .addField("段位名次", apexDto.getRankPlacement(), true)
                                .addField("Arena 段位", apexDto.getArenaRank(), true)
                                .setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : "")
                                .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                                .build()).complete(), member);
                    } catch (IOException e) {
                        onFailure(call, e);
                    }
                } else if (response.code() == 500) {
                    makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                            .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                            .setTitle(processComment(apexDto.getComments()))
                            .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
                            .addField("Origin ID", discordUser.getApexId(), false)
                            .addField("Apex tracker出错", "Apex tracker发生了系统问题，暂时无法读取你的战绩。待apex tracker解决他们的问题之后，你的组队命令才能显示战绩。", false)
                            .setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : "")
                            .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                            .build()).complete(), member);
                } else {
                    makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                            .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                            .setTitle(processComment(apexDto.getComments()))
                            .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
                            .addField("Origin ID", discordUser.getApexId(), false)
                            .addField("无法找到Origin账号", "Apex tracker无法搜索到你的Origin ID。如果你修改了Origin ID，请重新绑定。", false)
                            .setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : "")
                            .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                            .build()).complete(), member);
                }

            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                        .setAuthor(member.getEffectiveName() + " 请求Apex组队", null, member.getUser().getAvatarUrl())
                        .setTitle(processComment(apexDto.getComments()))
                        .setDescription(apexDto.getInviteUrl() == null ? apexDto.getInviteUrl() : "点击加入房间: [" + apexDto.getChannelName() + "](" + apexDto.getInviteUrl() + ")")
                        .addField("Origin ID", discordUser.getApexId(), false)
                        .addField("Apex tracker出错", "Apex tracker发生了系统问题，暂时无法读取你的战绩。待apex tracker解决他们的问题之后，你的组队命令才能显示战绩。", false)
                        .setFooter(apexDto.getInviteUrl() == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : "")
                        .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                        .build()).complete(), member);
            }
        });
    }

    public void invite(MessageChannel channel, Member member, String comments) {
        String inviteUrl = null;
        String channelName = null;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null) {
                Invite invite = voiceChannel.createInvite().complete();
                inviteUrl = invite.getUrl();
                channelName = voiceChannel.getName();
            }
        }

        DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(null);
        makeMessageCancelable(channel.sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(member.getEffectiveName() + " 请求组队", null, member.getUser().getAvatarUrl())
                .setTitle(processComment(comments))
                .setDescription(inviteUrl == null ? inviteUrl : "点击加入房间: [" + channelName + "](" + inviteUrl + ")")
                .setThumbnail("https://i.imgur.com/JCIxnvM.jpg")
                .setFooter(inviteUrl == null ? "在妖风电竞的任何语音频道使用本命令就可以自动生成上车链接。" : "")
                .setColor(shouldEmbedBePink(discordUser) ? pink : null)
                .build()).complete(), member);
    }

    public boolean isCancelable(String messageId, String userId) {
        return userId.equals(messageOwnership.get(messageId));
    }

    public void removeMessageId(String messageId) {
        messageOwnership.remove(messageId);
    }

    private void makeMessageCancelable(Message message, Member member) {
        message.addReaction("❌").queue();
        messageOwnership.put(message.getId(), member.getId());
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

    private boolean shouldEmbedBePink(DiscordUser discordUser) {
        return discordUser != null && discordUser.getRoles() != null && discordUser.getRoles().contains(pinkRoleId);
    }

    @Data
    @NoArgsConstructor
    static class ApexDto {
        private String comments = null;
        private String kills = "";
        private String rankName = "";
        private String rankPlacement = "";
        private String rankAvatar = "";
        private String arenaRank = "";
        private String damage = "";
        private String inviteUrl = null;
        private String channelName = null;
    }
}
