package com.tools.controller;

import com.tools.domain.CrlBorrowerLog;
import com.tools.domain.CrlEquipment;
import com.tools.repository.CrlBorrowerRepo;
import com.tools.repository.CrlEquipmentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/crl")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class CrlEquipmentController {
    private final CrlEquipmentRepo crlEquipmentRepo;
    private final CrlBorrowerRepo crlBorrowerRepo;

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

    @PostMapping("/borrow")
    public ResponseEntity<Object> borrow(@RequestBody CrlBorrowerLog crlBorrowerLog) {
        CrlBorrowerLog latestLog = crlBorrowerRepo.findFirstByEquipmentIdOrderByBorrowDateDesc(crlBorrowerLog.getEquipmentId());
        if (latestLog == null || latestLog.getReturnDate() != null) {
            // Borrowing equipment
            crlBorrowerLog.setId(0);
            crlBorrowerLog.setBorrowDate(Instant.now());
            crlBorrowerLog.setReturnDate(null);
            Optional<CrlEquipment> crlEquipmentOpt = crlEquipmentRepo.findById(crlBorrowerLog.getEquipmentId());
            if (crlEquipmentOpt.isPresent()) {
                CrlEquipment crlEquipment = crlEquipmentOpt.get();
                crlEquipment.setBorrower(crlBorrowerLog.getName());
                crlEquipmentRepo.save(crlEquipment);
                crlBorrowerRepo.save(crlBorrowerLog);
                return ResponseEntity.ok(crlBorrowerLog);
            } else {
                return ResponseEntity.badRequest().body("Referenced equipment is not found");
            }
        } else {
            if (latestLog.getUtEid().equals(crlBorrowerLog.getUtEid())) {
                // Returning equipment
                latestLog.setReturnDate(Instant.now());
                crlBorrowerLog.setReturnDate(Instant.now());
                crlBorrowerRepo.save(latestLog);
                crlEquipmentRepo.findById(latestLog.getEquipmentId()).ifPresent(crlEquipment -> {
                    crlEquipment.setBorrower(null);
                    crlEquipmentRepo.save(crlEquipment);
                });

                return ResponseEntity.ok(crlBorrowerLog);
            } else {
                return ResponseEntity.badRequest().body("This equipment is already borrowed by " + latestLog.getName() +
                        ". If you are returning this equipment, the EID you entered does not match our record.");
            }
        }
    }

}
