package com.tools.service.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;

@Component
public class AudioPlayerSendHandler implements AudioSendHandler {
    private AudioPlayerManager playerManager;
    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private TrackScheduler scheduler;

    @PostConstruct
    public void setup() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        audioPlayer = playerManager.createPlayer();
        scheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(scheduler);
    }

    public void loadAndPlay(String trackUrl) {
        playerManager.loadItemOrdered(scheduler, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("track loaded");
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack();

                if (track == null) {
                    track = playlist.getTracks().get(0);
                }

                System.out.println("playlistLoaded");
                scheduler.queue(track);
            }

            @Override
            public void noMatches() {
                System.out.println("nothing found");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("cannot play");
            }
        });
    }

    public void skip() {
        scheduler.nextTrack();
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
