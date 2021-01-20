package com.fmning.tools.controller;

import com.fmning.tools.domain.Email;
import com.fmning.tools.dto.EmailDto;
import com.fmning.tools.repository.EmailRepo;
import com.fmning.tools.service.EmailService;
import com.fmning.tools.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@RestController
@RequestMapping(value = "/api/email")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class EmailController {

    private final EmailRepo emailRepo;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailDto>> getEmails(@RequestParam(value = "page", defaultValue="0") int page,
                                                    @RequestParam(value = "size", defaultValue="15") int size){

        List<Email> emailList = emailRepo.findAllByOrderByIdDesc(PageRequest.of(page, size));

        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(emailRepo.count()))
                .body(emailList.stream().map(p -> modelMapper.map(p, EmailDto.class)).collect(Collectors.toList()));
    }

    @PostMapping
    @RequestMapping(value = "/send")
    public ResponseEntity<EmailDto> post(@RequestBody EmailDto emailDto, HttpServletRequest request) {

        Email email = modelMapper.map(emailDto, Email.class);
        email.setRequestAddress(WebUtil.getClientIpAddress(request));
        email.setRequestHeaders(WebUtil.getRequestHeaders(request));
        email.setRequestParams(WebUtil.getQueryParams(request));
        email.setCreated(Instant.now());

        emailService.send(email);

        return ResponseEntity.ok(emailDto);
    }

}
