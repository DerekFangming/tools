package com.fmning.tools.controller;

import com.fmning.tools.domain.Post;
import com.fmning.tools.repository.PostRepo;
import com.fmning.tools.dto.PostDto;
import com.fmning.tools.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@RestController
@RequestMapping(value = "/api/posts")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class PostController {

    private final ModelMapper modelMapper;
    private final PostService postService;
    private final PostRepo postRepo;

    private Timer timer = new Timer();

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam(value = "mode", required=false) String mode,
                                                  @RequestParam(value = "category", required=false) Integer category) {
        List<Post> postList;
        if (mode == null) mode = "all";
        if (category == null) category = 0;

        if (mode.equals("flagged")) {
            postList = postRepo.findByViewedAndFlagged(null, true, PageRequest.of(0, 10));
        } else if(mode.equals("ranked")) {
            postList = postRepo.findByViewedAndRankGreaterThan(null, 0, PageRequest.of(0, 10));
        } else {
            if (category == 0) {
                postList = postRepo.findByViewed(null, PageRequest.of(0, 10));
            } else {
                postList = postRepo.findByViewedAndCategory(null, category, PageRequest.of(0, 10));
            }
        }

        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(postRepo.countByViewed(null)))
                .body(postList.stream().map(p -> modelMapper.map(p, PostDto.class)).collect(Collectors.toList()));
    }

    @GetMapping("/reload")
    public ResponseEntity<Void> reload() {
        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        postService.loadPosts();
                    }
                },
                1000
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-read")
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

    @GetMapping("/cleanup")
    public ResponseEntity<Void> cleanup() throws Exception {
        postService.cleanupViewedPosts();
        return ResponseEntity.ok().build();
    }

    private String getNormalizedPath(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
}
