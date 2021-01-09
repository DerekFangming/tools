package com.tools.dto;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class YoutubeTrack {
    private String title;
    private String url;
    private AudioTrack track;
}
