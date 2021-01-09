package com.tools;

import com.tools.domain.Post;
import com.tools.dto.PostDto;
import com.tools.service.PostService;
import com.tools.service.discord.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
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
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public JDA jda(MessageReceivedEventListener messageReceivedEventListener, MemberJoinedEventListener memberJoinedEventListener,
                   MemberRemoveEventListener memberRemoveEventListener, MemberUpdateEventListener memberUpdateEventListener) throws Exception {
        return JDABuilder.createDefault(toolsProperties.getDcBotToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
//                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
//                .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                .addEventListeners(messageReceivedEventListener, memberJoinedEventListener, memberRemoveEventListener, memberUpdateEventListener)
                .build().awaitReady();
    }
}
