package com.tools.util;

import com.tools.service.PostService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class PostUrlConverter implements Converter<Integer, String> {

    private final PostService postService;

    @Override
    public String convert(MappingContext<Integer, String> context) {
        return postService.getPostUrl(context.getSource());
    }
}
