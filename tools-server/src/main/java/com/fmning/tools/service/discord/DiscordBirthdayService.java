package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fmning.tools.util.DiscordUtil.fromMember;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordBirthdayService {

    private final DiscordUserRepo discordUserRepo;

    private Pattern birthdayPattern = Pattern.compile("([0-9][0-9])-([0-3][0-9])");

    public void listSelf(MessageChannel channel, Member member) {
        DiscordUser user = discordUserRepo.findById(member.getId()).orElse(null);
        if (user != null && !StringUtils.isBlank(user.getBirthday())) {
            String[] birthday = user.getBirthday().split("-");
            channel.sendMessage("<@" + member.getId() + "> 你注册的生日是" + birthday[0] + "月" + birthday[1] + "日。").queue();
        } else {
            channel.sendMessage("<@" + member.getId() + "> 你尚未注册生日。").queue();
        }
    }

    public void getBirthday(MessageChannel channel, Member member, Member mentioned) {
        DiscordUser user = discordUserRepo.findById(mentioned.getId()).orElse(null);
        if (user != null && !StringUtils.isBlank(user.getBirthday())) {
            String[] birthday = user.getBirthday().split("-");
            channel.sendMessage("<@" + member.getId() + "> " + mentioned.getEffectiveName() + "的生日是" + birthday[0] + "月" + birthday[1] + "日。").queue();
        } else {
            channel.sendMessage("<@" + member.getId() + "> " + mentioned.getEffectiveName() + "尚未注册生日。").queue();
        }
    }

    public void listMonth(MessageChannel channel, String requestedMonth) {
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        try {
            int givenMonth = Integer.parseInt(requestedMonth);
            if (givenMonth > 0 && givenMonth < 13) month = givenMonth;
        } catch (Exception ignored){}
        List<DiscordUser> users = discordUserRepo.findByBirthdayStartingWithOrderByBirthdayAsc(String.format("%02d", month));
        if (users.size() == 0) {
            channel.sendMessage("**" + month + "月尚未有人注册生日**").queue();
        } else {
            channel.sendMessage("**" + month + "月已注册的生日**\n\n" + users.stream().map(u -> "**" + u.getBirthday() + ":** " + u.getNickname())
                    .collect(Collectors.joining("\n"))).queue();
        }
    }

    public void disable(MessageChannel channel, Member member) {
        discordUserRepo.findById(member.getId()).ifPresent(u -> {
            u.setBirthday(null);
            discordUserRepo.save(u);
            channel.sendMessage("<@" + member.getId() + "> 成功取消生日提醒").queue();
        });
    }

    public void register(MessageChannel channel, Member member, String birthday) {
        Matcher m = birthdayPattern.matcher(birthday);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            if (month > 0 && month < 13 && day > 0 && day < 32) {
                DiscordUser discordUser = discordUserRepo.findById(member.getId()).orElse(fromMember(member));
                discordUser.setBirthday(birthday);
                discordUserRepo.save(discordUser);

                String confirmation = "<@" + member.getId() + "> 成功注册生日为**" + month + "月" + day + "日**。";
                List<DiscordUser> users = discordUserRepo.findByBirthday(birthday);
                String sameDay = users.stream().filter(u -> !u.getId().equals(member.getId())).map(u -> "<@" + u.getId() + ">").collect(Collectors.joining("，"));

                if (sameDay.length() > 0) {
                    confirmation += "你和" + sameDay + "同一天生日！";
                }

                channel.sendMessage(confirmation).queue();
                return;
            }

        }
        channel.sendMessage("<@" + member.getId() + "> 无法识别" + birthday +
                "。生日格式必须为**月份-日期**， 比如**01-02** 或者 **11-29**").queue();
    }

}
