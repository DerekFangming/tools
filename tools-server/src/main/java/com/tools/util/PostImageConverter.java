package com.tools.util;

import com.tools.service.PostService;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PostImageConverter implements Converter<Integer, List<String>> {
    @Override
    public List<String> convert(MappingContext<Integer, List<String>> context) {
        List<String> imageNames = new ArrayList<>();
        File folder = new File(PostService.imgDir + context.getSource());
        if (folder.listFiles() != null) {
            for (File f : Objects.requireNonNull(folder.listFiles())) {
                String[] components = f.getAbsolutePath().replace("\\", "/").split("/");
                imageNames.add(components[components.length - 1]);
            }
        }

        return imageNames;
    }
}
