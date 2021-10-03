package com.fmning.tools.service.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordMusicService {
    private final AudioPlayerSendHandler audioPlayerSendHandler;

    public void join(Member member, AudioManager audioManager) {
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null) {
                audioManager.setSendingHandler(audioPlayerSendHandler);
                audioManager.openAudioConnection(voiceChannel);
            }
        }
    }

    public void play(MessageChannel channel, Member member, AudioManager audioManager, String keyword) {
        VoiceChannel voiceChannel = null;
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            voiceChannel = voiceState.getChannel();
        }
        if (voiceChannel == null) {
            channel.sendMessage("<@" + member.getId() + "> 你必须加入一个语音频道才能使用此指令。").queue();
            return;
        }
        audioManager.setSendingHandler(audioPlayerSendHandler);
        audioManager.openAudioConnection(voiceChannel);

        if (keyword == null) {
            channel.sendMessage("<@" + member.getId() + "> 请输入歌曲名。").queue();
        } else {
            audioPlayerSendHandler.loadAndPlay(keyword, channel, member.getId());
        }
    }

    public void say(MessageChannel channel, Member member, AudioManager audioManager, String sentence){
        if (audioPlayerSendHandler.isPlayingMusic()) {
            channel.sendMessage("<@" + member.getId() + "> 正在唱歌。只有唱完或者使用`yf stop" +
                    "`停止唱歌之后才能说话。").queue();
            return;
        }

        VoiceChannel voiceChannel = null;
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            voiceChannel = voiceState.getChannel();
        }
        if (voiceChannel == null) {
            channel.sendMessage("<@" + member.getId() + "> 你必须加入一个语音频道才能使用此指令。").queue();
            return;
        }
        audioManager.setSendingHandler(audioPlayerSendHandler);
        audioManager.openAudioConnection(voiceChannel);

        if (StringUtils.isEmpty(sentence)) {
            channel.sendMessage("<@" + member.getId() + "> 请输入要说的话。").queue();
        } else {
            audioPlayerSendHandler.say(sentence, channel, member.getId());
        }
    }

    public void skip() {
        audioPlayerSendHandler.skip();
    }

    public void stop() {
        audioPlayerSendHandler.stop();
    }

    public void showQueue(MessageChannel channel) {
        audioPlayerSendHandler.showQueue(channel);
    }

    public void toggleLoop(MessageChannel channel, Member member) {
        audioPlayerSendHandler.toggleLoop(channel, member.getId());
    }

}
