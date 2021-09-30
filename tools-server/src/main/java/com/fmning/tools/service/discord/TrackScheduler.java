package com.fmning.tools.service.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private boolean loop = false;
    private boolean playingMusic = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (track.getInfo().uri.startsWith("http")) {
            playingMusic = true;
        } else {
            player.stopTrack();
            queue.clear();
        }
        player.startTrack(track, true);
        queue.offer(track);
    }

    public void nextTrack() {
        loop = false;
        player.stopTrack();
        queue.poll();
        AudioTrack track = queue.peek();
        if (track != null) {
            player.startTrack(track, false);
        }
    }

    public void stop() {
        loop = false;
        player.stopTrack();
        queue.clear();
        playingMusic = false;
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public AudioTrack toggleLoop() {
        if (queue.size() > 0) {
            loop = !loop;
            return queue.peek();
        } else {
            return null;
        }
    }

    public boolean isLoop() {
        return loop;
    }

    public boolean isPlayingMusic() {
        return playingMusic;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!track.getInfo().uri.startsWith("http")) {
            try {
                Files.deleteIfExists(Paths.get(track.getInfo().uri));
            } catch (Exception ignored){}
        }

        if (endReason.mayStartNext) {
            if (queue.peek() != null && loop) {
                player.startTrack(Objects.requireNonNull(queue.peek()).makeClone(), false);
            } else {
                nextTrack();
            }
        } else {
            playingMusic = false;
        }
    }
}