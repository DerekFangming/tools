package com.fmning.tools.controller;

import com.fmning.tools.domain.Clip;
import com.fmning.tools.dto.ClipDto;
import com.fmning.tools.repository.ClipRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Timer;

@RestController
@RequestMapping(value = "/api/clipboard")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ClipboardController {

    private ClipDto clipDto;
    private Timer timer = new Timer();

    private final ClipRepo clipRepo;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        Clip clip = clipRepo.findTopByOrderByIdDesc();
        clipDto = clip == null ? new ClipDto() : modelMapper.map(clip, ClipDto.class);
    }

    @GetMapping
    public ResponseEntity<ClipDto> get() {
        return ResponseEntity.ok(clipDto);
    }

    @PostMapping
    public ResponseEntity<ClipDto> post(@RequestBody ClipDto clipDto) {

        timer.cancel();
        timer = new Timer();

        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        clipRepo.save(Clip.builder().content(clipDto.getContent()).created(Instant.now()).build());
                    }
                },
                30000
        );
        this.clipDto = clipDto;
        return ResponseEntity.ok(clipDto);
    }

}
