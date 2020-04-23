package com.tools;

import com.tools.domain.Post;
import com.tools.dto.PostDto;
import com.tools.util.PostImageConverter;
import com.tools.util.PostUrlConverter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class BeanConfig {

    private final PostImageConverter postImageConverter;
    private final PostUrlConverter postUrlConverter;

    @PostConstruct
    private void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Post.class, PostDto.class)
                .addMappings(mapper -> {
                    mapper.using(postImageConverter).map(Post::getId, PostDto::setImageNames);
                    mapper.using(postUrlConverter).map(Post::getId, PostDto::setUrl);
                });

        return modelMapper;
    }
}
