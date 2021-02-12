package com.fmning.tools.service.discord;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.*;
import com.fmning.tools.repository.*;
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
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.fmning.tools.util.DiscordUtil.fromMember;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordRoleService {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final ToolsProperties toolsProperties;
    private final OkHttpClient client;

    public void getRoleStatus(MessageChannel channel, Member member) {

        String message = getRoleStatus(member.getId(), false);
        if (message.length() == 0) {
            channel.sendMessage("<@" + member.getId() + "> 你当前没有Tag。使用yf help查看如何创建或分享Tag。").queue();
        } else {
            channel.sendMessage(new EmbedBuilder()
                    .setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
                    .setDescription(message)
                    .build()).queue();
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
            if (member.getTimeBoosted() == null && toolsProperties.isProduction()) {
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
            DiscordRoleMapping boostRole = discordRoleMappingRepo.findByOwnerIdAndType(user.getId(), DiscordRoleType.BOOST);
            if (boostRole == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                        .guildId(guild.getId())
                        .roleId(role.getId())
                        .enabled(true)
                        .code(RandomStringUtils.randomAlphanumeric(6))
                        .type(DiscordRoleType.BOOST)
                        .ownerId(user.getId())
                        .created(Instant.now())
                        .build());

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).queue();
                Role targetRole = guild.getRoleById(discordGuild.getRoleBoostRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).queue();
                channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(boostRole.getRoleId());
                if (role == null) channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                else {
                    role.getManager().setName(name).setColor(Color.decode(color)).queue();
                    channel.sendMessage("<@" + member.getId() + "> 专属tag **" + name + "**已更新成功。").queue();
                }
            }
        } else {
            DiscordRoleMapping levelRole = discordRoleMappingRepo.findByOwnerIdAndType(user.getId(), DiscordRoleType.LEVEL);
            if (levelRole == null) {
                Role role = guild.createRole()
                        .setColor(Color.decode(color))
                        .setHoisted(isBoostRole)
                        .setName(name)
                        .complete();
                discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                        .guildId(guild.getId())
                        .roleId(role.getId())
                        .enabled(true)
                        .code(RandomStringUtils.randomAlphanumeric(6))
                        .type(DiscordRoleType.LEVEL)
                        .ownerId(user.getId())
                        .created(Instant.now())
                        .build());

                //Add role to member and then move rank
                guild.addRoleToMember(member.getId(), role).complete();
                Role targetRole = guild.getRoleById(discordGuild.getRoleLevelRankRoleId());
                guild.modifyRolePositions().selectPosition(role).moveTo(targetRole.getPosition()).complete();
                channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已创建成功。").queue();
            } else {
                Role role = guild.getRoleById(levelRole.getRoleId());
                if (role == null) channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                else {
                    role.getManager().setName(name).setColor(Color.decode(color)).queue();
                    channel.sendMessage("<@" + member.getId() + "> tag **" + name + "**已更新成功。").queue();
                }
            }
        }
    }

    public void shareRole(MessageChannel channel, Member member, Member mentionedMember) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRoleMapping roleMapping = discordRoleMappingRepo.findByOwnerIdAndType(member.getId(), DiscordRoleType.LEVEL);
        if (roleMapping == null) {
            channel.sendMessage("<@" + member.getId() + "> 你没有等级tag可以分享，请先创建你的等级tag。").queue();
        } else if (member.getId().equals(mentionedMember.getId())) {
            channel.sendMessage("<@" + member.getId() + "> 不可以分享tag给自己。").queue();
        } else {
            DiscordRoleMapping existingMapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(mentionedMember.getId(), DiscordRoleType.SHARE, roleMapping.getRoleId());
            if (existingMapping != null) {
                if (existingMapping.isEnabled()) channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "已拥有你的等级tag。").queue();
                else if (member.getId().equals(existingMapping.getApproverId())) channel.sendMessage("<@" + member.getId() + "> " + "使用这个指令接受分享tag：\n`yf tag confirm " + existingMapping.getCode() + "`").queue();
                else channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要给你他的等级tag。使用这个指令接受这个tag：\n`yf tag confirm " + existingMapping.getCode() + "`").queue();
                return;
            }
            Guild guild = member.getGuild();
            Role role = guild.getRoleById(roleMapping.getRoleId());
            if (role == null) {
                channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                return;
            }

            String code = RandomStringUtils.randomAlphanumeric(6);
            discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                    .guildId(guild.getId())
                    .roleId(role.getId())
                    .enabled(false)
                    .code(code)
                    .type(DiscordRoleType.SHARE)
                    .ownerId(mentionedMember.getId())
                    .approverId(mentionedMember.getId())
                    .created(Instant.now())
                    .build());
            channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要给你tag **" +
                    role.getName() + "**。使用这个指令接受这个tag：\n`yf tag confirm " + code + "`").queue();
        }
    }

    public void requestRole(MessageChannel channel, Member member, Member mentionedMember) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRoleMapping roleMapping = discordRoleMappingRepo.findByOwnerIdAndType(mentionedMember.getId(), DiscordRoleType.LEVEL);
        if (roleMapping == null) {
            channel.sendMessage("<@" + member.getId() + "> " + mentionedMember.getEffectiveName() + "尚未创建等级tag，无法分享给你。").queue();
        } else if (member.getId().equals(mentionedMember.getId())) {
            channel.sendMessage("<@" + member.getId() + "> 不可以向自己请求tag。").queue();
        } else {
            DiscordRoleMapping existingMapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(member.getId(), DiscordRoleType.SHARE, roleMapping.getRoleId());
            if (existingMapping != null) {
                if (existingMapping.isEnabled()) channel.sendMessage("<@" + member.getId() + "> 你已拥有" + mentionedMember.getEffectiveName() + "的等级tag。").queue();
                else if (member.getId().equals(existingMapping.getApproverId())) channel.sendMessage("<@" + member.getId() + "> " + "使用这个指令接受这个tag：\n`yf tag confirm " + existingMapping.getCode() + "`").queue();
                else channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要你的等级tag。使用这个指令分享这个tag：\n`yf tag confirm " + existingMapping.getCode() + "`").queue();
                return;
            }
            Guild guild = member.getGuild();
            Role role = guild.getRoleById(roleMapping.getRoleId());
            if (role == null) {
                channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
                return;
            }

            String code = RandomStringUtils.randomAlphanumeric(6);
            discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                    .guildId(guild.getId())
                    .roleId(role.getId())
                    .enabled(false)
                    .code(code)
                    .type(DiscordRoleType.SHARE)
                    .ownerId(member.getId())
                    .approverId(mentionedMember.getId())
                    .created(Instant.now())
                    .build());
            channel.sendMessage("<@" + mentionedMember.getId() + "> " + member.getEffectiveName() + "想要你的等级tag **" +
                    role.getName() + "**。使用这个指令分享这个tag：\n`yf tag confirm " + code + "`").queue();
        }
    }

    public void confirmRole(MessageChannel channel, Member member, String code) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRoleMapping mapping = discordRoleMappingRepo.findByCode(code.trim());
        if (mapping == null) {
            channel.sendMessage("<@" + member.getId() + "> 验证码 **" + code + "**不存在。").queue();
            return;
        } else if (mapping.isEnabled() || !member.getId().equals(mapping.getApproverId())) {
            channel.sendMessage("<@" + member.getId() + "> 你无法使用这个验证码。").queue();
            return;
        }
        Guild guild = member.getGuild();
        Role role = guild.getRoleById(mapping.getRoleId());
        if (role != null) {
            guild.addRoleToMember(mapping.getOwnerId(), role).queue();
            mapping.setEnabled(true);
            mapping.setApproverId(null);
            discordRoleMappingRepo.save(mapping);
            channel.sendMessage("<@" + member.getId() + "> tag分享成功。").queue();
        } else {
            channel.sendMessage("<@" + member.getId() + "> tag分享失败，请联系管理员。").queue();
        }
    }

    public void showDeleteStatue(MessageChannel channel, Member member) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        String message = getRoleStatus(member.getId(), true);
        if (message.length() == 0) {
            channel.sendMessage("<@" + member.getId() + "> 你没有tag可以删除。").queue();
        } else {
            channel.sendMessage(new EmbedBuilder()
                    .setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
                    .setTitle("删除Tag请求")
                    .setDescription(message)
                    .build()).queue();
        }
    }

    private String getRoleStatus(String memberId, boolean delete) {
        String levelRoleId = null;

        String levelRole = "";
        String boostRole = "";
        String sharedRoleFromOthers = "";
        String sharedRoleToOthers = "";

        List<DiscordRoleMapping> roleMappings = discordRoleMappingRepo.findByOwnerId(memberId);
        for (DiscordRoleMapping rm : roleMappings) {
            if (rm.getType() == DiscordRoleType.LEVEL) {
                levelRoleId = rm.getRoleId();
                if (delete) {
                    levelRole = "**等级Tag：** 删除后所有拥有此tag的人都将失去这个tag\n`yf tag delete " + rm.getCode() +
                            "` 将删除 **" + discordRoleRepo.getNameById(rm.getRoleId()) + "**\n\n";
                } else {
                    levelRole = "**等级Tag：** " + discordRoleRepo.getNameById(rm.getRoleId()) + "\n\n";
                }
            } else if (rm.getType() == DiscordRoleType.BOOST) {
                if (delete) {
                    boostRole = "**Booster Tag：** 删除后你将失去单独的在线成员组\n`yf tag delete " + rm.getCode() +
                            "` 将删除 **" + discordRoleRepo.getNameById(rm.getRoleId()) + "**\n\n";
                } else {
                    boostRole = "**Booster Tag：** " + discordRoleRepo.getNameById(rm.getRoleId()) + "\n\n";
                }
            } else {
                if (delete) {
                    if (sharedRoleFromOthers.length() == 0) sharedRoleFromOthers = "**别人分享给你的Tag：** 删除后你自己将失去这个tag\n";
                    sharedRoleFromOthers += "`yf tag delete " + rm.getCode() + "` 将删除 **" + discordRoleRepo.getNameById(rm.getRoleId()) + "**";
                    if (!rm.isEnabled()) sharedRoleFromOthers += " (未接受)";
                    sharedRoleFromOthers += "\n";
                } else{
                    if (sharedRoleFromOthers.length() == 0) sharedRoleFromOthers = "**别人分享给你的Tag：** \n";
                    DiscordRole role = discordRoleRepo.findById(rm.getRoleId()).orElse(null);
                    DiscordRoleMapping mapping = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, rm.getRoleId());
                    String sharedFrom = mapping == null ? "" : discordUserRepo.getNicknameById(mapping.getOwnerId()) + " 分享给你 " ;
                    sharedRoleFromOthers += sharedFrom + "**" + (role == null ? null : role.getName()) + "**";
                    if (!rm.isEnabled()) sharedRoleFromOthers += " (未接受)";
                    sharedRoleFromOthers += "\n";
                }
            }
        }

        if (sharedRoleFromOthers.length() > 0) sharedRoleFromOthers += "\n";

        if (levelRoleId != null) {
            List<DiscordRoleMapping> sharedRoles = discordRoleMappingRepo.findAllByTypeAndRoleId(DiscordRoleType.SHARE, levelRoleId);
            for (DiscordRoleMapping rm : sharedRoles) {
                if (delete) {
                    if (sharedRoleToOthers.length() == 0) sharedRoleToOthers = "**你分享给别人的Tag：** 删除后对方将失去**" + discordRoleRepo.getNameById(levelRoleId) + "**\n";
                    sharedRoleToOthers += "`yf tag delete " + rm.getCode() + "` 将删除你分享给" + discordUserRepo.getNicknameById(rm.getOwnerId()) + "的等级Tag。";
                    if (!rm.isEnabled()) sharedRoleToOthers += " (未接受)";
                    sharedRoleToOthers += "\n";
                } else{
                    if (sharedRoleToOthers.length() == 0) sharedRoleToOthers = "**你把等级Tag" + discordRoleRepo.getNameById(levelRoleId) + "分享给了：**\n";
                    DiscordRoleMapping mapping = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, rm.getRoleId());
                    sharedRoleToOthers += mapping == null ? "无法读取" : discordUserRepo.getNicknameById(mapping.getOwnerId());
                    if (!rm.isEnabled()) sharedRoleToOthers += " (未接受)";
                    sharedRoleToOthers += "\n";
                }
            }

        }

        return levelRole + boostRole + sharedRoleFromOthers + sharedRoleToOthers;
    }

    public void deleteRole(MessageChannel channel, Member member, String code) {
        DiscordGuild discordGuild = discordGuildRepo.findById(member.getGuild().getId()).orElse(null);
        if (discordGuild == null || !discordGuild.isRoleEnabled()) return;

        DiscordRoleMapping roleMapping = discordRoleMappingRepo.findByCode(code);
        if (roleMapping == null) {
            channel.sendMessage("<@" + member.getId() + "> 验证码 **" + code + "**不存在。").queue();
        } else {
            if (!member.getId().equals(roleMapping.getOwnerId())) {
                if (roleMapping.getType() != DiscordRoleType.SHARE) {
                    channel.sendMessage("<@" + member.getId() + "> 你无法使用这个验证码。").queue();
                    return;
                }
                DiscordRoleMapping ownerMapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(member.getId(), DiscordRoleType.LEVEL, roleMapping.getRoleId());
                if (ownerMapping == null) {
                    channel.sendMessage("<@" + member.getId() + "> 你无法使用这个验证码。").queue();
                    return;
                }
            }

            Guild guild = member.getGuild();
            if (!roleMapping.isEnabled()) {
                discordRoleMappingRepo.delete(roleMapping);
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
            } else if (roleMapping.getType() == DiscordRoleType.SHARE) {
                Role role = guild.getRoleById(roleMapping.getRoleId());
                if (role != null) {
                    guild.removeRoleFromMember(roleMapping.getOwnerId(), role).queue();
                }
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
                discordRoleMappingRepo.delete(roleMapping);
            } else if (roleMapping.getType() == DiscordRoleType.LEVEL) {
                Role role = guild.getRoleById(roleMapping.getRoleId());
                if (role != null) {
                    role.delete().queue();
                } else if (discordRoleRepo.findById(roleMapping.getRoleId()).isPresent()) {
                    channel.sendMessage("<@" + member.getId() + "> 删除失败，请联系管理员。").queue();
                    return;
                }
                discordRoleMappingRepo.deleteByRoleId(roleMapping.getRoleId());
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
            } else if (roleMapping.getType() == DiscordRoleType.BOOST) {
                Role role = guild.getRoleById(roleMapping.getRoleId());
                if (role != null) {
                    role.delete().queue();
                } else if (discordRoleRepo.findById(roleMapping.getRoleId()).isPresent()) {
                    channel.sendMessage("<@" + member.getId() + "> 删除失败，请联系管理员。").queue();
                    return;
                }
                discordRoleMappingRepo.delete(roleMapping);
                channel.sendMessage("<@" + member.getId() + "> tag删除成功。").queue();
            } else {
                channel.sendMessage("<@" + member.getId() + "> 系统错误，请联系管理员。").queue();
            }
        }
    }
}
