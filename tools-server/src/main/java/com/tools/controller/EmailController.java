package com.tools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.domain.Email;
import com.tools.dto.EmailDto;
import com.tools.dto.PostDto;
import com.tools.repository.EmailRepo;
import com.tools.service.EmailService;
import com.tools.type.EmailSenderType;
import com.tools.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.bind.annotation.*;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.tools.util.WebUtil.TOTAL_COUNT;

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
