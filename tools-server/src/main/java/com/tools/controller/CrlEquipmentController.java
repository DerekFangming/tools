package com.tools.controller;

import com.tools.domain.CrlEquipment;
import com.tools.repository.CrlEquipmentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/crl")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class CrlEquipmentController {
    private final CrlEquipmentRepo crlEquipmentRepo;

    @GetMapping("/equipment")
    public ResponseEntity<List<CrlEquipment>> getEquipments() {
        return ResponseEntity.ok(StreamSupport.stream(crlEquipmentRepo.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    @PostMapping("/equipment")
    public ResponseEntity<CrlEquipment> postEquipments(@RequestBody CrlEquipment crlEquipment) {
        crlEquipment.setId(0);
        crlEquipment.setCreated(Instant.now());
        crlEquipment.setBorrower(null);
        crlEquipmentRepo.save(crlEquipment);
        return ResponseEntity.ok(crlEquipment);
    }
}
