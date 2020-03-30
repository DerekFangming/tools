package com.tools.controller;

import com.tools.dto.ClipboardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/clipboard")
public class ClipboardController {

    private ClipboardDto clipboardDto = new ClipboardDto();

    @GetMapping
    public ResponseEntity<ClipboardDto> get() {
        return ResponseEntity.ok(clipboardDto);
    }

    @PostMapping
    public ResponseEntity<ClipboardDto> post(@RequestBody ClipboardDto clipboardDto) {
        this.clipboardDto = clipboardDto;
        return ResponseEntity.ok(clipboardDto);
    }

}
