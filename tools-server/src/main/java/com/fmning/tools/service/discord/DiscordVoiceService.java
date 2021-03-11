package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordUser;
import com.fmning.tools.repository.DiscordUserRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordVoiceService {

    private final DiscordUserRepo discordUserRepo;

    private static final int LOTTERY_MINUTE = 30;
    private static final int MAX_LOTTERY_CHANCE_LIMIT = 12 * 60 / LOTTERY_MINUTE;

    public void memberJoinChannel(VoiceChannel joinedChannel, VoiceChannel afkChannel, Member member) {

        discordUserRepo.findById(member.getId()).ifPresent(u -> {
            if (joinedChannel.getId().equals(afkChannel.getId())) {
                stopCounting(u);
            } else {
                startCounting(u);
            }
        });
    }

    public void memberLeaveChannel(Member member) {
        discordUserRepo.findById(member.getId()).ifPresent(this::stopCounting);
    }

    private void startCounting(DiscordUser user) {
        if (user.getVoiceLastJoin() == null) {
            user.setVoiceLastJoin(Instant.now());
            discordUserRepo.save(user);
        }
    }

    private void stopCounting(DiscordUser user) {
        if (user.getVoiceLastJoin() != null) {
            Instant lastJoin = user.getVoiceLastJoin();
            int minutes = (int) ChronoUnit.MINUTES.between(lastJoin, Instant.now());
            user.setVoiceLastJoin(null);
            user.setVoiceMinutes(user.getVoiceMinutes() + minutes);

            int lotteryChance = minutes / LOTTERY_MINUTE;
            if (lotteryChance > MAX_LOTTERY_CHANCE_LIMIT) lotteryChance = MAX_LOTTERY_CHANCE_LIMIT;
            user.setLotteryChance(user.getLotteryChance() + lotteryChance);
            discordUserRepo.save(user);
        }
    }

}
