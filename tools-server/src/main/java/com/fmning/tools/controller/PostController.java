package com.fmning.tools.controller;

import com.fmning.tools.domain.Post;
import com.fmning.tools.repository.PostRepo;
import com.fmning.tools.dto.PostDto;
import com.fmning.tools.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam(value = "mode", required=false) String mode,
                                                  @RequestParam(value = "category", required=false) Integer category) {

        Specification<Post> spec = (Specification<Post>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (mode != null) {
                if (mode.equals("flagged")) {
                    predicates.add(criteriaBuilder.isTrue(root.get("flagged")));
                } else if (mode.equals("ranked")) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("rank"), 0));
                } else if (mode.equals("saved")) {
                    predicates.add(criteriaBuilder.isTrue(root.get("saved")));
                }
            }

            if (category != null && category != 0) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (!"saved".equals(mode)) {
                predicates.add(criteriaBuilder.isNull(root.get("viewed")));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Post> page = postRepo.findAll(spec, PageRequest.of(0, 10));

        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(page.getTotalElements()))
                .body(page.getContent().stream().map(p -> modelMapper.map(p, PostDto.class)).collect(Collectors.toList()));
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

    @PutMapping("/mark-unsaved")
    public ResponseEntity<Void> markPostsAsUnsaved(@RequestBody List<Integer> idList) {
        if (idList != null && idList.size() > 0) {
            postRepo.markAsUnsaved(idList);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-saved")
    public ResponseEntity<Void> markPostsAsSaved(@RequestBody Integer id) {
        postRepo.findById(id).ifPresent(p -> {
            p.setSaved(true);
            postRepo.save(p);
        });
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
