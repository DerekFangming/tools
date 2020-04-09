package com.tools.controller;

import com.tools.domain.Post;
import com.tools.dto.PostDto;
import com.tools.type.HtmlReaderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/post")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class PostController {

    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        log.info("aaa");
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts() throws Exception {

        String root = "D:/Github/imgs/";
        List<PostDto> postList = new ArrayList<>();
        for (int i = 1; i < 4; i ++) {
            List<String> imageNames = new ArrayList<>();
            File folder = new File(root + i);
            for (File f : folder.listFiles()) {
                String[] components = getNormalizedPath(f).split("/");
                imageNames.add(components[components.length - 1]);
            }
            postList.add(new PostDto(i, "Some title " + i, imageNames));
        }

        return ResponseEntity.ok(postList);
    }

    @GetMapping("/1")
    public ResponseEntity<PostDto> getPosts1() throws Exception {

		Post post = Post.builder()
				.title("test title")
				.imgUrls("urls")
				.attachment("attachment")
				.htmlType(HtmlReaderType.JSOUP)
				.html("html")
				.exception("exception")
				.created(Instant.now())
				.viewed(Instant.now())
				.rank(1)
				.category(123)
				.flagged(true)
				.build();

        return ResponseEntity.ok(modelMapper.map(post, PostDto.class));
    }

    private String getNormalizedPath(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
}
