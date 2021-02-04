package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordGuild;
import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.domain.DiscordRoleRequest;
import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordGuildRepo;
import com.fmning.tools.repository.DiscordRoleRepo;
import com.fmning.tools.repository.DiscordRoleRequestRepo;
import com.fmning.tools.repository.DiscordUserRepo;
import com.fmning.tools.type.DiscordRoleRequestType;
import com.fmning.tools.type.DiscordRoleType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fmning.tools.util.DiscordUtil.fromMember;
import static com.fmning.tools.util.DiscordUtil.fromRole;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordRoleService {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleRequestRepo discordRoleRequestRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final OkHttpClient client;

    public void getRoleStatus(MessageChannel channel, Member member) {
        DiscordUser user = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
        StringBuilder sb = new StringBuilder("<@" + member.getId() + ">");
        List<DiscordRole> roles = discordRoleRepo.findByOwnerId(user.getId());

        DiscordRole levelRole = roles.stream().filter(r -> r.getType() == DiscordRoleType.LEVEL).findFirst().orElse(null);
        DiscordRole boostRole = roles.stream().filter(r -> r.getType() == DiscordRoleType.BOOST).findFirst().orElse(null);

        if (levelRole == null && boostRole == null) {
            channel.sendMessage(sb.append(" 你尚未拥有任何tag。").toString()).queue();
        } else {
            sb.append(" 你当前拥有");
            if (levelRole != null) sb.append("等级tag **").append(levelRole.getName()).append("**");
            if (boostRole != null) {
                if (levelRole != null) sb.append(" 和");
                sb.append("Server Booster专属tag **").append(boostRole.getName()).append("**");
            }

            channel.sendMessage(sb.append("。").toString()).queue();
        }
    }

    public void createUpdateRole(MessageChannel channel, Member member, String color, String name, boolean isBoostRole) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        for(String n : discordGuild.getRoleNameBlacklist().split("\\,+")) {
            if (name.toLowerCase().contains(n.toLowerCase())) {
                channel.sendMessage("<@" + member.getId() + "> tag名字不能包含字符**" + n + "**。").queue();
                return;
            }
        }

        for(String c : discordGuild.getRoleColorBlacklist().split("\\,+")) {
            if (color.toLowerCase().contains(c.toLowerCase())) {
                channel.sendMessage("<@" + member.getId() + "> tag颜色不可以是**" + c + "**。").queue();
                return;
            }
        }

        color = color.startsWith("#") ? color : "#" + color;
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
                createUpdateAndAssignRole(member, channel, discordGuild, name, color, isBoostRole);
            }
        } else {
            Request request = new Request.Builder().url("https://mee6.xyz/api/plugins/levels/leaderboard/392553285971869697?limit=500").build();

            Call call = client.newCall(request);
            String finalColor = color;
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
                                createUpdateAndAssignRole(member, channel, discordGuild, name.trim(), finalColor, isBoostRole);
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
            DiscordRole boostRole = discordRoleRepo.findByOwnerIdAndType(user.getId(), DiscordRoleType.BOOST);
            if (boostRole == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                DiscordRole discordRole = discordRoleRepo.findById(role.getId()).orElse(fromRole(role));
                discordRole.setType(DiscordRoleType.BOOST);
                discordRole.setOwnerId(user.getId());
                discordRoleRepo.save(discordRole);

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).queue();
                Role targetRole = guild.getRoleById(discordGuild.getRoleBoostRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).queue();
                channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(boostRole.getId());
                role.getManager().setName(name).setColor(Color.decode(color)).queue();
                channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已更新成功。").queue();
            }
        } else {
            DiscordRole levelRole = discordRoleRepo.findByOwnerIdAndType(user.getId(), DiscordRoleType.LEVEL);
            if (levelRole == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                DiscordRole discordRole = discordRoleRepo.findById(role.getId()).orElse(fromRole(role));
                discordRole.setType(DiscordRoleType.LEVEL);
                discordRole.setOwnerId(user.getId());
                discordRoleRepo.save(discordRole);

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).complete();
                Role targetRole = guild.getRoleById(discordGuild.getRoleLevelRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).complete();
                channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(levelRole.getId());
                role.getManager().setName(name).setColor(Color.decode(color)).queue();
                channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已更新成功。").queue();
            }
        }
    }

    public void shareRole(MessageChannel channel, Member member, Member mentionedMember) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRole levelRole = discordRoleRepo.findByOwnerIdAndType(member.getId(), DiscordRoleType.LEVEL);
        if (levelRole == null) {
            channel.sendMessage("<@" + member.getId() + "> 你没有等级tag可以分享，请先创建你的等级tag。").queue();
        } else {
            Guild guild = member.getGuild();
            Role role = guild.getRoleById(levelRole.getId());
            if (mentionedMember.getRoles().stream().anyMatch(r -> r.getId().equals(levelRole.getId()))) {
                channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "已拥有你的等级tag **" + role.getName() + "**。").queue();
                return;
            }

            String key = RandomStringUtils.randomAlphanumeric(6);
            discordRoleRequestRepo.save(DiscordRoleRequest.builder()
                    .id(key)
                    .guildId(guild.getId())
                    .roleId(levelRole.getId())
                    .action(DiscordRoleRequestType.SHARE)
                    .requesterId(member.getId())
                    .approverId(mentionedMember.getId())
                    .created(Instant.now())
                    .build());
            channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要给你tag **" +
                    role.getName() + "**。使用这个指令接受这个tag：\n`yf tag confirm " + key + "`").queue();
        }
    }

    public void requestRole(MessageChannel channel, Member member, Member mentionedMember) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

//        DiscordUser user = discordUserRepo.findById(mentionedMember.getId()).orElse(null);
        DiscordRole levelRole = discordRoleRepo.findByOwnerIdAndType(mentionedMember.getId(), DiscordRoleType.LEVEL);
        if (levelRole == null) {
            channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "尚未创建等级tag，无法分享给你。").queue();
        } else {
            Guild guild = member.getGuild();
            Role role = guild.getRoleById(levelRole.getId());
            if (member.getRoles().stream().anyMatch(r -> r.getId().equals(levelRole.getId()))) {
                channel.sendMessage("<@" + member.getId() + "> 你已拥有" + mentionedMember.getEffectiveName() + "的等级tag **" + role.getName() + "**。").queue();
                return;
            }

            String key = RandomStringUtils.randomAlphanumeric(6);
            discordRoleRequestRepo.save(DiscordRoleRequest.builder()
                    .id(key)
                    .guildId(guild.getId())
                    .roleId(levelRole.getId())
                    .action(DiscordRoleRequestType.REQUEST)
                    .requesterId(member.getId())
                    .approverId(mentionedMember.getId())
                    .created(Instant.now())
                    .build());
            channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要你的等级tag **" +
                    role.getName() + "**。使用这个指令分享这个tag：\n`yf tag confirm " + key + "`").queue();
        }
    }

    public void confirmRole(MessageChannel channel, Member member, String code) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRoleRequest request = discordRoleRequestRepo.findById(code.trim()).orElse(null);
        if (request == null) {
            channel.sendMessage("<@" + member.getId() + "> 验证码 **" + code + "**不存在。").queue();
            return;
        } else if (!request.getApproverId().equals(member.getId())) {
            channel.sendMessage("<@" + member.getId() + "> 你无法使用这个验证码。").queue();
            return;
        }

        Guild guild = member.getGuild();
        if (request.getAction() == DiscordRoleRequestType.SHARE) {
            Role role = guild.getRoleById(request.getRoleId());
            if (role != null) {
                guild.addRoleToMember(member, role).queue();
                discordRoleRequestRepo.delete(request);
                channel.sendMessage("<@" + member.getId() + "> tag分享成功。").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> tag分享失败，请联系管理员。").queue();
            }
        } else if (request.getAction() == DiscordRoleRequestType.REQUEST) {
            Role role = guild.getRoleById(request.getRoleId());
            if (role != null) {
                guild.addRoleToMember(request.getRequesterId(), role).queue();
                discordRoleRequestRepo.delete(request);
                channel.sendMessage("<@" + member.getId() + "> tag分享成功。").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> tag分享失败，请联系管理员。").queue();
            }
        } else if (request.getAction() == DiscordRoleRequestType.DELETE) {
            Role role = guild.getRoleById(request.getRoleId());
            if (role != null) {
                role.delete().queue();
                discordRoleRequestRepo.delete(request);
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> tag删除失败，请联系管理员。").queue();
            }
        } else if (request.getAction() == DiscordRoleRequestType.REMOVE) {
            Role role = guild.getRoleById(request.getRoleId());
            if (role != null) {
                guild.removeRoleFromMember(member, role).queue();
                discordRoleRequestRepo.delete(request);
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> tag删除 失败，请联系管理员。").queue();
            }
        }
    }

    public void deleteRole(MessageChannel channel, Member member) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        List<Role> sharedRoles = member.getRoles().stream().filter(r -> {
            DiscordRole role = discordRoleRepo.findById(r.getId()).orElse(null);
            return role != null && role.getOwnerId() != null && !role.getOwnerId().equalsIgnoreCase(member.getId());
        }).collect(Collectors.toList());

        Guild guild = member.getGuild();
        StringBuilder sb = new StringBuilder();
        List<DiscordRoleRequest> requestList = new ArrayList<>();
        DiscordRole levelRole = discordRoleRepo.findByOwnerIdAndType(member.getId(), DiscordRoleType.LEVEL);
        if (levelRole != null) {
            String key = RandomStringUtils.randomAlphanumeric(6);
            requestList.add(DiscordRoleRequest.builder()
                    .id(key)
                    .guildId(guild.getId())
                    .roleId(levelRole.getId())
                    .action(DiscordRoleRequestType.DELETE)
                    .approverId(member.getId())
                    .created(Instant.now())
                    .build());
            sb.append("**等级Tag：** 删除后所有拥有此tag的人都将失去这个tag\n`yf tag confirm ").append(key).append("` 将删除 **")
                    .append(levelRole.getName()).append("**\n\n");
        }

        DiscordRole boostRole = discordRoleRepo.findByOwnerIdAndType(member.getId(), DiscordRoleType.BOOST);
        if (boostRole != null) {
            String key = RandomStringUtils.randomAlphanumeric(6);
            requestList.add(DiscordRoleRequest.builder()
                    .id(key)
                    .guildId(guild.getId())
                    .roleId(boostRole.getId())
                    .action(DiscordRoleRequestType.DELETE)
                    .approverId(member.getId())
                    .created(Instant.now())
                    .build());
            sb.append("**Booster Tag：** 删除后你将失去单独的在线成员组\n`yf tag confirm ").append(key).append("` 将删除 **")
                    .append(boostRole.getName()).append("**\n\n");
        }

        if (sharedRoles.size() > 0) {
            sb.append("**分享给你的Tag：** 删除后你自己将失去这个tag\n");
            for (Role r : sharedRoles) {
                String key = RandomStringUtils.randomAlphanumeric(6);
                requestList.add(DiscordRoleRequest.builder()
                        .id(key)
                        .guildId(guild.getId())
                        .roleId(r.getId())
                        .action(DiscordRoleRequestType.REMOVE)
                        .approverId(member.getId())
                        .created(Instant.now())
                        .build());
                sb.append("`yf tag confirm ").append(key).append("` 将删除 **").append(r.getName()).append("**\n");
            }
        }

        String description = sb.toString();
        if (description.length() == 0) {
            channel.sendMessage("<@" + member.getId() + "> 你没有tag可以删除。").queue();
        } else {
            discordRoleRequestRepo.saveAll(requestList);
            channel.sendMessage(new EmbedBuilder()
                    .setTitle("删除Tag")
                    .setDescription(description)
                    .build()).queue();
        }
    }
}
