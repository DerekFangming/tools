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
import com.tools.ToolsProperties;
import com.tools.dto.YoutubeTrack;
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
        scheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(scheduler);
    }

    public void loadAndPlay(String keyword, MessageChannel channel, String userId) {

        try {
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
            JSONObject snippet = firstResult.getJSONObject("snippet");


            String trackUrl = "https://www.youtube.com/watch?v=" + firstResult.getJSONObject("id").getString("videoId");
            String title = snippet.getString("title");
            playerManager.loadItemOrdered(scheduler, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    channel.sendMessage("<@" + userId + "> 歌曲**" + title + "**已加入播放队列。").queue();
                    scheduler.queue(YoutubeTrack.builder()
                            .title(title)
                            .url(trackUrl)
                            .track(track)
                            .build());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
//                AudioTrack track = playlist.getSelectedTrack();
//
//                if (track == null) {
//                    track = playlist.getTracks().get(0);
//                }
//
//                System.out.println("playlistLoaded");
//                scheduler.queue(track);
                }

                @Override
                public void noMatches() {
                    channel.sendMessage("<@" + userId + "> 无法找到歌曲**" + title + "**。").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    channel.sendMessage("<@" + userId + "> 无法播放歌曲**" + title + "**。").queue();
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

    public void showQueue(MessageChannel channel) {
        List<YoutubeTrack> youtubeTrackList = scheduler.getQueue();
        if (youtubeTrackList.size() == 0) {
            channel.sendMessage(new EmbedBuilder()
                    .setTitle("当前播放队列")
                    .setDescription("当前播放队列中没有歌曲。使用以下指令添加歌曲到播放列表。\n`yf play 关键词或者Youtube网址`")
                    .build()).queue();
        } else {
            int count = 1;
            StringBuilder description = new StringBuilder();
            for (YoutubeTrack t : youtubeTrackList) {
                description.append(count).append(". ");
                description.append("[").append(t.getTitle()).append("](").append(t.getUrl()).append(")");
                if (count == 1) {
                    description.append("（正在播放）");
                }
                description.append("\n");
                count ++;
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
