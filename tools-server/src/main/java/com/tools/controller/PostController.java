package com.tools.controller;

import com.tools.dao.PostRepo;
import com.tools.domain.Post;
import com.tools.dto.PostDto;
import com.tools.service.PostService;
import com.tools.type.HtmlReaderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping(value = "/api/posts")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class PostController {

    private final ModelMapper modelMapper;
    private final PostService postService;
    private final PostRepo postRepo;

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam("mode") String mode) {

        List<Post> postList;

        if (mode.equals("flagged")) {
            postList = postRepo.findByViewedAndFlagged(null, true, PageRequest.of(0, 10));
        } else if(mode.equals("ranked")) {
            postList = postRepo.findByViewedAndRankGreaterThan(null, 0, PageRequest.of(0, 10));
        } else {
            postList = postRepo.findByViewed(null, PageRequest.of(0, 10));
        }

        List<PostDto> postDtoList = new ArrayList<>();

        for (Post p : postList) {
            List<String> imageNames = new ArrayList<>();
            File folder = new File(PostService.imgDir + p.getId());
            if (folder.listFiles() != null) {
                for (File f : Objects.requireNonNull(folder.listFiles())) {
                    String[] components = getNormalizedPath(f).split("/");
                    imageNames.add(components[components.length - 1]);
                }
            }

            PostDto dto = modelMapper.map(p, PostDto.class);
            dto.setImageNames(imageNames);
            dto.setUrl(postService.getPostUrl(p.getId()));

            postDtoList.add(dto);
        }

        return ResponseEntity.ok(postDtoList);
    }

    @GetMapping("/reload")
    public ResponseEntity<Void> reload() {
        postService.loadPosts();
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> markPostsAsRead(@RequestBody List<Integer> idList) {
        if (idList != null && idList.size() > 0) {
            postRepo.markAsRead(Instant.now(), idList);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/debug")
    public ResponseEntity<Void> debugPosts(@RequestBody List<Integer> idList) {
        postService.debugPosts(idList);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/1")
    public ResponseEntity<Void> getPosts1() throws Exception {

        File folder = new File("D:/Github/test/img/13201942");
        boolean deleted = folder.delete();

        FileUtils.deleteDirectory(folder);
        System.out.println(deleted);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/2")
    public ResponseEntity<Void> getPosts2() throws Exception {

        postService.cleanupViewedPosts();

        return ResponseEntity.ok().build();
    }

    private String getNormalizedPath(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
}
