package com.fmning.tools.service.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.*;
import com.fmning.tools.dto.DiscordObjectDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.type.DiscordTaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.fmning.tools.util.DiscordUtil.toHexString;

@Service
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordService extends BaseEventListener {

    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;
    private final JDA jda;
    private final ToolsProperties toolsProperties;
    private final ObjectMapper objectMapper;
    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordCategoryRepo discordCategoryRepo;
    private final DiscordChannelRepo discordChannelRepo;
    private final DiscordAchievementRepo discordAchievementRepo;
    private final DiscordTaskRepo discordTaskRepo;

    public static String speedMessageId = "";
    public static String roleId = "";
    public static String channelId = "";
    private int showSeconds = 30;
    private int baseDelaySeconds = 30;
    private int randDelaySeconds = 5;
    private Timer timer = new Timer();
    private boolean started;

    @PostConstruct
    public void init() {
        if (toolsProperties.isProduction()) {
            roleId = "";
            channelId = "";
        } else {
            roleId = "793670567940718622";
            channelId = "792772167959314493";
        }

//        jda.upsertCommand("ping", "Calculate ping of the bot").queue();

//        Guild g = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
//        g.retrieveCommands().complete().forEach(c -> {
//            g.deleteCommandById(c.getId()).complete();
//        });

//        g.upsertCommand("ar1-2", "Apex rank 1等2").queue();
//        g.upsertCommand("ar2-1", "Apex rank 2等1").queue();
//        g.upsertCommand("an1-2", "Apex normal 1等2").queue();
//        g.upsertCommand("an2-1", "Apex normal 2等1").queue();
//        g.upsertCommand("a1", "Apex normal 2等1").queue();

//        jda.getGuildById(toolsProperties.getDcDefaultGuildId()).up
    }

    public boolean startSpeed() {
        if (!started) {
            Role role = jda.getRoleById(roleId);
            TextChannel channel = jda.getTextChannelById(channelId);
            if (role != null && channel != null) {
                int delay = (new Random().nextInt(randDelaySeconds) * 1000);
                timer.schedule(new SpeedTask(), baseDelaySeconds * 1000 + delay);
                started = true;
                channel.sendMessage(new EmbedBuilder()
                        .setTitle("拼手速小活动开始！")
                        .setDescription("活动期间你将在这个频道看到类似这样的消息，随机出现，每次出现" + showSeconds +
                                "秒。在此期间，点击消息下方 \uD83D\uDC4D 表情即可获得活动tag " + role.getName() + "\n")
                        .build()).queue(r -> r.addReaction("\uD83D\uDC4D").queue());
            }
        }
        return started;
    }

    public boolean stopSpeed() {
        if (started) {
            started = false;
        }
        return started;
    }

    class SpeedTask extends TimerTask {

        @Override
        public void run() {
            int delay = (new Random().nextInt(randDelaySeconds) * 1000);
            if (started) timer.schedule(new SpeedTask(), baseDelaySeconds * 1000 + delay);

            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(new EmbedBuilder()
                        .setTitle("点击下方大拇指表情获得手速Tag")
                        .setDescription("▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓███▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓▓██▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▒▒███▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓███▒▒█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓███▒▒█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓██▒░▒█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓░▒▒█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓▒▒▒█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓▒▒▒██▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓▒▒▒▒██▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n" +
                                "▒▓█▓▒▒▒▒▒██████████████████████▓▒▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▓█████████████████████▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▒▒░░░░░░░░░░░░░. ▓█▓▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▒▒▓▓▓▓▓▓▓▓▓▓▓▓▓███▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▓█████████████████▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒██░░░░░░░░░░░░░░▓█▓▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▓█▓▓▓▓▓▓▓▓▓▓▓▓▓▓██▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▒█████████████████▒▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒██░░░░░░░░░░░░░░▓█▓▒\n" +
                                "▒▓█▓▒▒▒▒▒▒▒▒▒▒▒▓█▓▒▓▓▓▓▓▓▓▓▓▓▓▓██▒▒\n" +
                                "▒▒██▒▒▒▒▒▒▒▒▒▒▒▒█████████████████▒▒\n" +
                                "▒▒▓██▒▒▒▒▒▒▒▒▒▒██▒░▒▒▒▒▒▒▒▒▒▒░░▓█▓▒\n" +
                                "▒▒▒▓███▓▓▓▓▓▓▓▒██▓▒▓▓▓▓▓▓▓▓▓▒▒▒██▓▒\n" +
                                "▒▒▒▒▒▓██████████████████████████▒▒▒")
                        .build()).queue(r -> {
                    speedMessageId = r.getId();
                    r.addReaction("\uD83D\uDC4D").queue(rr -> {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                channel.deleteMessageById(r.getId()).queue();
                            }
                        }, showSeconds * 1000);
                    });
                });
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void announceBirthDay() {
        try {
            discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId()).ifPresent(g -> {
                if (g.isBirthdayEnabled()) {
                    Guild guild = jda.getGuildById(g.getId());
                    MessageChannel channel = guild.getTextChannelById(g.getBirthdayChannelId());

                    Calendar today = Calendar.getInstance();
                    String birthday = String.format("%02d-%02d", today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));

                    String names = null;
                    String ids = null;
                    List<DiscordUser> birthdayUsers = discordUserRepo.findByBirthday(birthday);
                    for (DiscordUser u : birthdayUsers) {
                        if (names == null) {
                            names = u.getName();
                            ids = u.getId();
                        } else {
                            names += ", " + u.getName();
                            ids += "> <@" + u.getId();
                        }
                        try {
                            guild.addRoleToMember(u.getId(), jda.getRoleById(g.getBirthdayRoleId())).queue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (names != null && channel != null) {
                        String message = replacePlaceHolder(g.getBirthdayMessage(), names, ids);
                        channel.sendMessage("@here " + message).queue();
                    }

                    Calendar yesterday = Calendar.getInstance();
                    yesterday.add(Calendar.DAY_OF_MONTH, -1);
                    String passedBirthday = String.format("%02d-%02d", yesterday.get(Calendar.MONTH) + 1, yesterday.get(Calendar.DAY_OF_MONTH));
                    discordUserRepo.findByBirthday(passedBirthday).forEach(d -> {
                        try {
                            guild.removeRoleFromMember(d.getId(), jda.getRoleById(g.getBirthdayRoleId())).queue();
                        } catch (Exception ignored){}
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void cleanUpRoleRequests() {
        discordRoleMappingRepo.deleteByCreated(Instant.now().minus(1, ChronoUnit.DAYS));
    }

    @Scheduled(cron= "0 0/30 * * * ?")
    public void cleanupTempChannel() {
        Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
        if (guild != null) {
            discordUserRepo.findByTempChannelIdNotNull().forEach(u -> {
                VoiceChannel vc = guild.getVoiceChannelById(u.getTempChannelId());
                if (vc == null) {
                    u.setTempChannelId(null);
                    discordUserRepo.save(u);
                } else if (vc.getMembers().size() == 0){
                    vc.delete().complete();
                    u.setTempChannelId(null);
                    discordUserRepo.save(u);
                }
            });
        }
    }


    @Scheduled(cron= "0 */3 * * * *")
    public void checkDiscordTasks() {
        discordTaskRepo.findByTimeoutBefore(Instant.now()).forEach(t -> {
            try {
                if (t.getType() == DiscordTaskType.UN_MUTE) {
                    Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
                    if (guild != null) {
                        Role role = guild.getRoleById(toolsProperties.getMutedToleId());
                        if (role != null) guild.removeRoleFromMember(t.getPayload(), role).queue();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                discordTaskRepo.delete(t);
            }
        });
    }

    public List<DiscordObjectDto> getTextChannels(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            return guild.getTextChannels()
                    .stream()
                    .map(tc -> DiscordObjectDto.builder()
                            .id(tc.getId())
                            .name(tc.getName())
                            .build())
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<DiscordObjectDto> getRoles(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            return guild.getRoles()
                    .stream()
                    .map(r -> DiscordObjectDto.builder()
                            .id(r.getId())
                            .name(r.getName() )
                            .build())
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void seedRoles(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.getRoles().forEach(r -> {
                DiscordRole role = discordRoleRepo.findById(r.getId()).orElse(null);
                if (role == null) {
                    discordRoleRepo.save(DiscordRole.builder()
                            .id(r.getId())
                            .guildId(toolsProperties.getDcDefaultGuildId())
                            .name(r.getName())
                            .color(toHexString(r.getColor()))
                            .position(r.getPositionRaw())
                            .created(Instant.from(r.getTimeCreated())).build());
                }
            });
        }
    }

    public void seedChannels(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.getCategories().forEach(cat -> {
                DiscordCategory discordCategory = discordCategoryRepo.findById(cat.getId()).orElse(null);
                if (discordCategory == null) {
                    discordCategoryRepo.save(DiscordCategory.builder()
                            .id(cat.getId())
                            .guildId(guildId)
                            .name(cat.getName())
                            .position(cat.getPositionRaw())
                            .created(Instant.from(cat.getTimeCreated()))
                            .build());
                }

                cat.getChannels().forEach(channel -> {
                    DiscordChannel discordChannel = discordChannelRepo.findById(channel.getId()).orElse(null);
                    if (discordChannel == null) {
                        discordChannelRepo.save(DiscordChannel.builder()
                                .id(channel.getId())
                                .guildId(guildId)
                                .categoryId(cat.getId())
                                .name(channel.getName())
                                .type(channel.getType())
                                .position(channel.getPositionRaw())
                                .created(Instant.from(channel.getTimeCreated()))
                                .build());
                    }

                });
            });
        }
    }

    public void seedMembers(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.getMembers().forEach(m -> {
                DiscordUser user = discordUserRepo.findById(m.getId()).orElse(null);
                if (user == null) {
                    String roles;
                    try {
                        roles = objectMapper.writeValueAsString(m.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        roles = "";
                    }
                    discordUserRepo.save(DiscordUser.builder()
                            .id(m.getId())
                            .name(m.getUser().getName())
                            .guildId(guildId)
                            .nickname(m.getEffectiveName())
                            .avatarId(m.getUser().getAvatarId())
                            .roles(roles)
                            .createdDate(Instant.from(m.getUser().getTimeCreated()))
                            .joinedDate(Instant.from(m.getTimeJoined()))
                            .boostedDate(m.getTimeBoosted() == null ? null : Instant.from(m.getTimeBoosted()))
                            .build());
                }
            });
        }
    }

    public void addRole(String memberId, String roleId) {
        Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
        Member member = guild.getMemberById(memberId);
        if (member != null) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                guild.addRoleToMember(member, role).complete();
            } else {throw new IllegalArgumentException("Null role " + roleId);}
        } else {throw new IllegalArgumentException("Null member " + memberId);}
    }

    public void removeRole(String memberId, String roleId) {
        Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
        Member member = guild.getMemberById(memberId);
        if (member != null) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                guild.removeRoleFromMember(member, role).queue();
            } else {throw new IllegalArgumentException("Null role " + roleId);}
        } else {throw new IllegalArgumentException("Null member " + memberId);}
    }

    public void moveRole(String roleId, int position) {
        if (position == 0) throw new IllegalArgumentException("Invalid position " + position);
        Guild guild = jda.getGuildById(toolsProperties.getDcDefaultGuildId());
        Role role = guild.getRoleById(roleId);
        if (role != null) {
            guild.modifyRolePositions().selectPosition(role).moveTo(position).complete();
        } else {throw new IllegalArgumentException("Null role " + roleId);}
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void dailyCalculateScore() {
        calculateScore();
    }

    public void calculateScore() {
        int pageInd = 0;
        int limit = 50;
        Page<DiscordUser> page = null;
        Instant now = Instant.now();
        while (page == null || page.getNumberOfElements() > 0) {
            page = discordUserRepo.findAll(PageRequest.of(pageInd, limit, Sort.by(Sort.Direction.ASC, "id")));
            for (DiscordUser discordUser : page.getContent()) {
                long daysJoined = 0;
                long daysBoosted = 0;
                if (discordUser.getJoinedDate() != null) daysJoined = ChronoUnit.DAYS.between(discordUser.getJoinedDate(), now);
                if (discordUser.getBoostedDate() != null) daysBoosted = ChronoUnit.DAYS.between(discordUser.getBoostedDate(), now);

                long levelPoint = 0;
                long rankPoint = 0;
                if (discordUser.getRoles() != null) {
                    if (discordUser.getRoles().contains("802126882585182238")) levelPoint = 1000;// 顶级
                    else if (discordUser.getRoles().contains("802613742646853632")) levelPoint = 600;// 特级
                    else if (discordUser.getRoles().contains("802650000189947974")) levelPoint = 350;// 高级
                    else if (discordUser.getRoles().contains("802650002995806218")) levelPoint = 200;// 中级
                    else if (discordUser.getRoles().contains("803212908896583731")) levelPoint = 100;// 初级

                    if (discordUser.getRoles().contains("784949763685875723")) rankPoint = 1000;// 猎杀
                    else if (discordUser.getRoles().contains("784949724721578034")) rankPoint = 600;// 大师
                    else if (discordUser.getRoles().contains("784949693499179048")) rankPoint = 350;// 钻石
                    else if (discordUser.getRoles().contains("784949655406510122")) rankPoint = 200;// 白金
                }

                long achievementPoint = 0;
                if (!StringUtils.isEmpty(discordUser.getAchievements())) {
                    try {
                        List<Integer> achievementIds = objectMapper.readValue(discordUser.getAchievements(), new TypeReference<List<Integer>>() {});
                        for (int achievementId : achievementIds) {
                            DiscordAchievement discordAchievement = discordAchievementRepo.findById(achievementId).orElse(null);
                            if (discordAchievement != null) {
                                achievementPoint += discordAchievement.getScore();
                            }
                        }

                    } catch (Exception ignored) {}
                }

                long total = daysJoined + daysBoosted * 2 + discordUser.getVoiceMinutes() / 100 + levelPoint + rankPoint + achievementPoint;

                discordUser.setScore((int)total);
                discordUserRepo.save(discordUser);
            }

            if (page.getNumberOfElements() < limit) break;
            pageInd ++;
        }
    }

}
