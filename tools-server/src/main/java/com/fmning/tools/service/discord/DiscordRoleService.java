package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordGuild;
import com.fmning.tools.domain.DiscordRoleRequest;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordRoleRequestRepo;
import com.fmning.tools.repository.DiscordUserRepo;
import com.fmning.tools.type.DiscordRoleRequestType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fmning.tools.service.discord.MessageReceivedEventListener.userMentionPattern;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordRoleService {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleRequestRepo discordRoleRequestRepo;
    private final OkHttpClient client;

    private Pattern userMentionIdPattern = Pattern.compile("<@!(.*?)>");

    public void createUpdateRole(String[] command, MessageChannel channel, Member member, boolean isBoostRole) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        if (command.length == 2) {
            DiscordUser user = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
            StringBuilder sb = new StringBuilder("<@" + member.getId() + ">");

            String levelRoleName = null;
            if (user.getLevelRoleId() != null) {
                Role role = member.getGuild().getRoleById(user.getLevelRoleId());
                levelRoleName = role.getName();
            }

            String boostRoleName = null;
            if (user.getBoostRoleId() != null) {
                Role role = member.getGuild().getRoleById(user.getBoostRoleId());
                boostRoleName = role.getName();
            }

            if (levelRoleName == null && boostRoleName == null) {
                channel.sendMessage(sb.append(" 你尚未拥有任何tag。").toString()).queue();
                return;
            } else {
                sb.append(" 你当前拥有");
                if (levelRoleName != null) sb.append("等级tag **").append(levelRoleName).append("**");
                if (boostRoleName != null) {
                    if (levelRoleName != null) sb.append(" 和");
                    sb.append("Server Booster专属tag **").append(boostRoleName).append("**");
                }

                channel.sendMessage(sb.append("。").toString()).queue();
                return;
            }
        }

        if (command.length < 4) {
            channel.sendMessage("<@" + member.getId() + "> 无法识别tag指令。使用yf help查看如何创建tag。").queue();
            return;
        }

        for(String n : discordGuild.getRoleNameBlacklist().split("\\,+")) {
            if (command[2].toLowerCase().contains(n)) {
                channel.sendMessage("<@" + member.getId() + "> tag名字不能包含字符**" + n + "**。").queue();
                return;
            }
        }

        for(String c : discordGuild.getRoleColorBlacklist().split("\\,+")) {
            if (command[3].toLowerCase().contains(c)) {
                channel.sendMessage("<@" + member.getId() + "> tag颜色不可以是**" + c + "**。").queue();
                return;
            }
        }

        String color = command[3].startsWith("#") ? command[3] : "#" + command[3];
        try {
            Color.decode(color);
        } catch (Exception ignored) {
            channel.sendMessage("<@" + member.getId() + "> 颜色必须是hex颜色代码，比如**D03210**，不区分大小写。你可以去这个网站上挑选颜色hex代码。https://htmlcolorcodes.com/color-picker/").queue();
            return;
        }

        if (isBoostRole) {
            if (member.getTimeBoosted() == null) {
                channel.sendMessage("<@" + member.getId() + "> 只有Server Booster才可以创建专属tag。").queue();
            } else {
                createUpdateAndAssignRole(member, channel, discordGuild, command[2].trim(), color, isBoostRole);
            }
        } else {
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
                            if (player.getInt("level") < discordGuild.getRoleLevelRequirement()) {
                                channel.sendMessage("<@" + member.getId() + "> 等级超过" + discordGuild.getRoleLevelRequirement() + "级才可以创建tag。").queue();
                                return;
                            } else if (player.getString("id").equals(member.getId())) {
                                createUpdateAndAssignRole(member, channel, discordGuild, command[2].trim(), color, isBoostRole);
                                return;
                            }
                        }
                        channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                    } catch (IOException e) {
                        onFailure(call, e);
                    }
                }

                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    channel.sendMessage("<@" + member.getId() + "> 无法读取你的等级，请稍后再试。你可以去这里确认的你等级。https://mee6.xyz/api/plugins/levels/leaderboard/392553285971869697").queue();
                }
            });
        }
    }

    private void createUpdateAndAssignRole(Member member, MessageChannel channel, DiscordGuild discordGuild, String name, String color, boolean isBoostRole) {
        Guild guild = member.getGuild();
        DiscordUser user = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        if (isBoostRole) {
            if (user.getBoostRoleId() == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                user.setBoostRoleId(role.getId());
                discordUserRepo.save(user);

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).queue();
                Role targetRole = guild.getRoleById(discordGuild.getRoleBoostRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).queue();
                channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(user.getBoostRoleId());
                role.getManager().setName(name).setColor(Color.decode(color)).queue();
                channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已更新成功。").queue();
            }
        } else {
            if (user.getLevelRoleId() == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                user.setLevelRoleId(role.getId());
                discordUserRepo.save(user);

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).queue();
                Role targetRole = guild.getRoleById(discordGuild.getRoleLevelRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).queue();
                channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(user.getLevelRoleId());
                role.getManager().setName(name).setColor(Color.decode(color)).queue();
                channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已更新成功。").queue();
            }
        }
    }

    public void shareRole(String[] command, MessageChannel channel, Member member, List<Member> mentions) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        if (command.length < 3) {
            channel.sendMessage("<@" + member.getId() + "> 无法识别tag指令。使用yf help查看如何分享tag。").queue();
            return;
        }

        if (mentions.size() == 0) {
            channel.sendMessage("<@" + member.getId() + "> 无法识别tag指令。使用yf help查看如何分享tag。").queue();
            return;
        }

        DiscordUser user = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        if (user.getLevelRoleId() == null) {
            channel.sendMessage("<@" + member.getId() + "> 你没有等级tag可以分享，请先创建你的等级tag。").queue();
        } else {
            Guild guild = member.getGuild();
            Role role = guild.getRoleById(user.getLevelRoleId());
            Member mentionedMember = mentions.get(0);
            if (mentionedMember.getRoles().stream().anyMatch(r -> r.getId().equals(user.getLevelRoleId()))) {
                channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "已拥有你的等级tag **" + role.getName() + "**。").queue();
                return;
            }

            String key = RandomStringUtils.randomAlphanumeric(6);
            discordRoleRequestRepo.save(DiscordRoleRequest.builder()
                    .id(key)
                    .guildId(guild.getId())
                    .roleId(user.getLevelRoleId())
                    .action(DiscordRoleRequestType.SHARE)
                    .requesterId(user.getId())
                    .approverId(mentionedMember.getId())
                    .created(Instant.now())
                    .build());
            channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要给你tag **" +
                    role.getName() + "**。使用这个指令接受这个tag：\n`yf confirmTag " + key + "`").queue();
        }
    }

    public void confirmRole(String[] command, MessageChannel channel, Member member) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        if (command.length < 3) {
            channel.sendMessage("<@" + member.getId() + "> 无法识别tag指令。使用yf help查看如何分享tag。").queue();
            return;
        }

        DiscordRoleRequest request = discordRoleRequestRepo.findById(command[2].trim()).orElse(null);
        if (request == null) {
            channel.sendMessage("<@" + member.getId() + "> 验证码 **" + command[2] + "**不存在。").queue();
            return;
        } else if (!request.getApproverId().equals(member.getId())) {
            channel.sendMessage("<@" + member.getId() + "> 你无法使用这个验证码。").queue();
            return;
        }

        Guild guild = member.getGuild();
        if (request.getAction() == DiscordRoleRequestType.SHARE) {
            Role role = guild.getRoleById(request.getRoleId());
            if (role != null) guild.addRoleToMember(member, role).queue();
            discordRoleRequestRepo.delete(request);
            channel.sendMessage("<@" + member.getId() + "> tag分享成功。").queue();
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
}
