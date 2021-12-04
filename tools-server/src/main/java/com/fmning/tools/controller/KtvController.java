package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.KtvSong;
import com.fmning.tools.repository.KtvSongRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/ktv")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class KtvController {

    private final KtvSongRepo ktvSongRepo;
    private final ToolsProperties toolsProperties;

    @GetMapping
    public List<KtvSong> listSongs(@RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "page", defaultValue = "0") int page) {

        Specification<KtvSong> spec = (Specification<KtvSong>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("requested"), false));
            if (keyword != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("title")), "%" + keyword.trim().toUpperCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<KtvSong> ktvPage = ktvSongRepo.findAll(spec, PageRequest.of(page, 50));
        return ktvPage.getContent();
    }

    @PostMapping
    public KtvSong requestSong(@RequestBody KtvSong ktvSong){
        if (ktvSong.getId() != 0) {
            throw new IllegalArgumentException("Invalid request");
        } else if (StringUtils.isEmpty(ktvSong.getTitle().trim())) {
            throw new IllegalArgumentException("Please provide song name");
        }

        ktvSong.setRequested(true);
        ktvSongRepo.save(ktvSong);
        return ktvSong;
    }

    @GetMapping("/sync")
    public void load() {
        String path = toolsProperties.isProduction() ? "/media/media/ktv" : "C:\\Users\\synfm\\Downloads";
        File baseDir = new File(path);
        if (!baseDir.exists()) {
            throw new IllegalArgumentException("Directory does not exist");
        } else if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not directory");
        }

        // Delete all songs and reload
        ktvSongRepo.deleteAllSongs();
        processSongsInDirectory(baseDir);
    }

    private void processSongsInDirectory(File dir) {
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(f -> {
            if (f.isDirectory()) {
                processSongsInDirectory(f);
                return;
            }

            if (f.getName().toLowerCase().endsWith("mkv")) {
                ktvSongRepo.save(KtvSong.builder()
                        .title(f.getName().substring(0, f.getName().length() - 4))
                        .requested(false)
                        .build());
            }
        });
    }

}
