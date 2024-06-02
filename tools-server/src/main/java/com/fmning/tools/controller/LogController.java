package com.fmning.tools.controller;

import com.fmning.tools.domain.Log;
import com.fmning.tools.dto.EmailDto;
import com.fmning.tools.repository.LogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@RestController
@RequestMapping(value = "/api/logs")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class LogController {

    private final LogRepo logRepo;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Log>> listLogs(@RequestParam(value = "service", required = false) String service, @RequestParam(value = "level", required = false) String level,
                              @RequestParam(value = "message", required = false) String message,
                              @RequestParam(value = "page", defaultValue = "0") int page) {

        Specification<Log> spec = (Specification<Log>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (service != null) {
                predicates.add(criteriaBuilder.equal(root.get("service"), service));
            }
            if (level != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }
            if (message != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("message")), "%" + message.trim().toUpperCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Log> logPage = logRepo.findAll(spec, PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "created")));
        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(logPage.getTotalElements()))
                .body(logPage.getContent());
    }
}
