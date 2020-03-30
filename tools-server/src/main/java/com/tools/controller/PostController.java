package com.tools.controller;

import com.tools.dto.PostDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/post")
public class PostController {

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

    private String getNormalizedPath(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
}
