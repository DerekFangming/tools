package com.fmning.tools.service.discord;

import com.fmning.tools.ToolsProperties;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.MessageChannel;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class AudioPlayerSendHandler implements AudioSendHandler {

    private final OkHttpClient client;
    private final ToolsProperties toolsProperties;

    private AudioPlayerManager playerManager;
    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private TrackScheduler scheduler;

    @PostConstruct
    public void setup() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        audioPlayer = playerManager.createPlayer();
        audioPlayer.setFrameBufferDuration(5000);
        scheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(scheduler);
    }

    public void loadAndPlay(String keyword, MessageChannel channel, String userId) {

        try {
            String trackUrl;
            if (keyword.contains("www.youtube.com") || keyword.contains("youtu.be")) {
                trackUrl = keyword;
            } else {
                Request request = new Request.Builder()
                        .url(HttpUrl.parse("https://www.googleapis.com/youtube/v3/search").newBuilder()
                                .addQueryParameter("part", "snippet")
                                .addQueryParameter("maxResults", "1")
                                .addQueryParameter("type", "video")
                                .addQueryParameter("key", toolsProperties.getYoutubeApiKey())
                                .addQueryParameter("q", keyword)
                                .build())
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
                JSONArray items = json.getJSONArray("items");
                JSONObject firstResult = items.getJSONObject(0);
                trackUrl = "https://www.youtube.com/watch?v=" + firstResult.getJSONObject("id").getString("videoId");
            }

            playerManager.loadItemOrdered(scheduler, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    channel.sendMessage("<@" + userId + "> 歌曲**" + track.getInfo().title + "**已加入播放队列。").queue();
                    scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    channel.sendMessage("<@" + userId + "> " + playlist.getTracks().size() + "首歌曲已加入播放队列。").queue();
                    playlist.getTracks().forEach(t -> scheduler.queue(t));
                }

                @Override
                public void noMatches() {
                    channel.sendMessage("<@" + userId + "> 无法找到歌曲**" + keyword + "**。").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    channel.sendMessage("<@" + userId + "> 无法播放歌曲**" + keyword + "**。").queue();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            //TODO
        }
    }

    public void skip() {
        scheduler.nextTrack();
    }

    public void stop() {
        scheduler.stop();
    }

    public void toggleLoop(MessageChannel channel, String userId) {
        AudioTrack audioTrack = scheduler.toggleLoop();
        if (audioTrack == null) {
            channel.sendMessage("<@" + userId + "> 当前没有正在播放的歌曲。先使用`yf play`播放歌曲后再使用`loop`指令。").queue();
        } else {
            if (scheduler.isLoop()) {
                channel.sendMessage("<@" + userId + "> 正在循环播放歌曲**" + audioTrack.getInfo().title + "**").queue();
            } else {
                channel.sendMessage("<@" + userId + "> 已停止循环播放歌曲**" + audioTrack.getInfo().title + "**").queue();
            }
        }
    }

    public void showQueue(MessageChannel channel) {
        List<AudioTrack> audioTracks = scheduler.getQueue();
        if (audioTracks.size() == 0) {
            channel.sendMessage(new EmbedBuilder()
                    .setTitle("当前播放队列")
                    .setDescription("当前播放队列中没有歌曲。使用以下指令添加歌曲到播放列表。\n`yf play 关键词或者Youtube网址`")
                    .build()).queue();
        } else {
            int count = 1;
            StringBuilder description = new StringBuilder();
            for (AudioTrack t : audioTracks) {
                StringBuilder song = new StringBuilder();
                song.append(count).append(". ");
                song.append("[").append(t.getInfo().title).append("](").append(t.getInfo().uri).append(")");
                if (count == 1) {
                    if (scheduler.isLoop()) {
                        song.append("（正在循环播放）");
                    } else {
                        song.append("（正在播放）");
                    }
                }
                song.append("\n");

                if (description.length() + song.length() < 2000) {
                    description.append(song);
                    count++;
                } else {
                    description.append("\n还有").append(audioTracks.size() - count + 1).append("首歌曲。");
                    break;
                }
            }

            channel.sendMessage(new EmbedBuilder()
                    .setTitle("当前播放队列")
                    .setDescription(description)
                    .build()).queue();
        }
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
