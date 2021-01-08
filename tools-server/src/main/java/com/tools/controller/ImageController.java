package com.tools.controller;

import com.tools.domain.Image;
import com.tools.dto.EmailDto;
import com.tools.repository.ImageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/images")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class ImageController {

    private final ImageRepo imageRepo;

    @PostMapping
    @RequestMapping(value = "/bulk")
    public ResponseEntity<List<Image>> bulkCreate(@RequestBody List<Image> images) {
        images = images.stream()
                .peek(i -> {
                    i.setId(0);
                    i.setCreated(Instant.now());
                })
                .collect(Collectors.toList());

        imageRepo.saveAll(images);
        return ResponseEntity.ok(images);
    }

    @GetMapping
    public ResponseEntity<List<Image>> getImages(){
        return ResponseEntity.ok(imageRepo.findAllByOrderByIdDesc());
    }

}
