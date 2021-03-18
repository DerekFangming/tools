package com.fmning.tools.service.discord;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.DiscordGuild;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

import static com.fmning.tools.util.DiscordUtil.fromMember;
import static net.dv8tion.jda.api.Permission.ALL_PERMISSIONS;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordChannelService {

    private final DiscordUserRepo discordUserRepo;
    private final DiscordGuildRepo discordGuildRepo;
    private final ToolsProperties toolsProperties;
    private final OkHttpClient client;

    public void getChannelStatus(MessageChannel channel, Member member) {
        DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        if (discordUser.getTempChannelId() == null && discordUser.getBoostChannelId() == null) {
            channel.sendMessage("<@" + member.getId() + "> 你当前没有频道。使用yf help查看如何创建频道。").queue();
        } else {
            StringBuilder sb = new StringBuilder();
            Guild guild = member.getGuild();
            if (discordUser.getBoostChannelId() != null) {
                VoiceChannel vc = guild.getVoiceChannelById(discordUser.getBoostChannelId());
                if (vc != null) {
                    sb.append("booster私人频道**").append(vc.getName()).append("**");
                }
            }
            if (discordUser.getTempChannelId() != null) {
                VoiceChannel vc = guild.getVoiceChannelById(discordUser.getTempChannelId());
                if (vc != null) {
                    if (sb.length() > 0) sb.append("和");
                    sb.append("临时频道频道**").append(vc.getName()).append("**");
                }
            }
            channel.sendMessage("<@" + member.getId() + "> 你当前拥有" + sb.toString() + "。").queue();
        }
    }

    public void createChannel(MessageChannel channel, Member member, String channelName, boolean isBoostChannel) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isChannelEnabled()) return;

        for(String n : discordGuild.getRoleNameBlacklist().split("\\,+")) {
            if (channelName.toLowerCase().contains(n.toLowerCase())) {
                channel.sendMessage("<@" + member.getId() + "> 频道名字不能包含字符**" + n + "**。").queue();
                return;
            }
        }

        if (isBoostChannel) {
            if (member.getTimeBoosted() == null && toolsProperties.isProduction()) {
                channel.sendMessage("<@" + member.getId() + "> 只有Server Booster才可以创建私人频道。").queue();
                return;
            }
        }
        Request request = new Request.Builder().url("https://mee6.xyz/api/plugins/levels/leaderboard/392553285971869697?limit=500").build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() != 200) {
                    onFailure(call, new IOException());
                    return;
                }
                try {
                    JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());

                    JSONArray players = json.getJSONArray("players");
                    for (int i = 0; i < players.length(); i++) {
                        JSONObject player = players.getJSONObject(i);
                        if (player.getInt("level") < 5) {
                            channel.sendMessage("<@" + member.getId() + "> 等级超过5级才可以创建" +
                                    (isBoostChannel ? "私人" : "临时") + "频道。").queue();
                            return;
                        } else if (player.getString("id").equals(member.getId())) {
                            createUpdateChannel(member, channel, discordGuild, channelName.trim(), isBoostChannel);
                            return;
                        }
                    }
                    channel.sendMessage("<@" + member.getId() + "> 系统错误，请稍后再试。").queue();
                } catch (IOException e) {
                    onFailure(call, e);
                }
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                channel.sendMessage("<@" + member.getId() + "> 无法读取你的等级，请稍后再试。你可以去这里确认的你等级。https://mee6.xyz/api/plugins/levels/leaderboard/392553285971869697").queue();
            }
        });
    }

    private void createUpdateChannel(Member member, MessageChannel channel, DiscordGuild discordGuild, String channelName, boolean isBoostChannel) {
        Guild guild = member.getGuild();
        DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        if ((isBoostChannel && discordUser.getBoostChannelId() != null) || (!isBoostChannel && discordUser.getTempChannelId() != null)) {
            VoiceChannel vc = guild.getVoiceChannelById(isBoostChannel ? discordUser.getBoostChannelId() : discordUser.getTempChannelId());
            if (vc != null) {
                vc.getManager().setName(channelName).queue();
                if (isBoostChannel) {
                    channel.sendMessage("<@" + member.getId() + "> 私人频道更新成功。").queue();
                } else {
                    Invite invite = vc.createInvite().complete();
                    channel.sendMessage("<@" + member.getId() + "> 临时频道更新成功。" + invite.getUrl()).queue();
                }
                return;
            }
        }

        Category category = guild.getCategoryById(isBoostChannel ? discordGuild.getChannelBoostCatId() : discordGuild.getChannelTempCatId());
        if (category != null) {
            VoiceChannel vc = isBoostChannel ? category.createVoiceChannel(channelName)
                    .addPermissionOverride(member, ALL_PERMISSIONS, 0)
                    .addPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK, Permission.VOICE_STREAM, Permission.VOICE_USE_VAD,
                            Permission.PRIORITY_SPEAKER),
                            EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.CREATE_INSTANT_INVITE, Permission.VOICE_CONNECT, Permission.VOICE_MUTE_OTHERS,
                                    Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS))
                    .complete() : category.createVoiceChannel(channelName).complete();
            if (isBoostChannel) {
                discordUser.setBoostChannelId(vc.getId());
                discordUserRepo.save(discordUser);
                channel.sendMessage("<@" + member.getId() + "> 私人频道创建成功。").queue();
            } else {
                discordUser.setTempChannelId(vc.getId());
                discordUserRepo.save(discordUser);
                Invite invite = vc.createInvite().complete();
                channel.sendMessage("<@" + member.getId() + "> 临时频道创建成功。" + invite.getUrl()).queue();
            }
        } else {
            channel.sendMessage("<@" + member.getId() + "> 系统错误，请稍后再试。").queue();
        }
    }

    public void deleteChannel(MessageChannel channel, Member member, boolean isBoostChannel) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isChannelEnabled()) return;

        DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        if ((isBoostChannel && discordUser.getBoostChannelId() != null) || (!isBoostChannel && discordUser.getTempChannelId() != null)) {
            Guild guild = member.getGuild();
            VoiceChannel vc = guild.getVoiceChannelById(isBoostChannel ? discordUser.getBoostChannelId() : discordUser.getTempChannelId());
            if (vc != null) {
                vc.delete().queue();
            }
            if (isBoostChannel) {
                discordUser.setBoostChannelId(null);
                discordUserRepo.save(discordUser);
                channel.sendMessage("<@" + member.getId() + "> 私人频道删除成功。").queue();
            } else {
                discordUser.setTempChannelId(null);
                discordUserRepo.save(discordUser);
                channel.sendMessage("<@" + member.getId() + "> 临时频道删除成功。").queue();
            }
        } else {
            channel.sendMessage("<@" + member.getId() + "> 你没有" +
            (isBoostChannel ? "私人" : "临时") + "频道可以删除。").queue();
        }
    }

}
