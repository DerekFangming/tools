package com.tools;

import com.google.gson.JsonArray;
import com.tools.domain.Post;
import com.tools.dto.PostDto;
import com.tools.service.PostService;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class BeanConfig {

    private final PostService postService;
    private final ToolsProperties toolsProperties;

    @PostConstruct
    private void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Post.class, PostDto.class)
                .addMappings(mapper -> {
                    mapper.using((Converter<Integer, List<String>>) context -> {
                        List<String> imageNames = new ArrayList<>();
                        File folder = new File(PostService.IMG_DIR + context.getSource());
                        if (folder.listFiles() != null) {
                            for (File f : Objects.requireNonNull(folder.listFiles())) {
                                String[] components = f.getAbsolutePath().replace("\\", "/").split("/");
                                imageNames.add(components[components.length - 1]);
                            }
                        }

                        return imageNames;
                    }).map(Post::getId, PostDto::setImageNames);

                    mapper.using((Converter<String, List<String>>) context -> {
                        if (context.getSource() == null) {
                            return Collections.emptyList();
                        } else {
                            return Arrays.asList(context.getSource().split(","));
                        }
                    }).map(Post::getImgUrls, PostDto::setImageUrls);

                    mapper.using((Converter<Integer, String>) context -> postService.getPostUrl(context.getSource())).map(Post::getId, PostDto::setUrl);
                });

        return modelMapper;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(toolsProperties.getDcBotToken())
                .build()
                .login()
                .block();
    }
}
