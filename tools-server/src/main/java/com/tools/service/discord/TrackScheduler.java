package com.tools.service.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private boolean loop = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        player.startTrack(track, true);
        queue.offer(track);
    }

    public void nextTrack() {
        player.stopTrack();
        queue.poll();
        AudioTrack track = queue.peek();
        if (track != null) {
            player.startTrack(track, false);
        }
    }

    public void stop() {
        player.stopTrack();
        queue.clear();
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

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (queue.peek() != null && loop) {
                player.startTrack(Objects.requireNonNull(queue.peek()).makeClone(), false);
            } else {
                nextTrack();
            }
        }
    }
}