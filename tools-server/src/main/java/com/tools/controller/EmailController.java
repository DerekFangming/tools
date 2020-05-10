package com.tools.controller;

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
import org.springframework.web.bind.annotation.*;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/email")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class EmailController {

    private final EmailRepo emailRepo;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<EmailDto>> getEmails(@RequestParam(value = "type", required=false) String type){
        List<Email> emailList = emailRepo.findAllByOrderByIdDesc(PageRequest.of(1, 10));
        return ResponseEntity.ok()//postRepo.countByViewed(null)
                .header("X-Total-Count", String.valueOf(1))
                .body(emailList.stream().map(p -> modelMapper.map(p, EmailDto.class)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Void> post(@RequestBody EmailDto emailDto, HttpServletRequest request) {

        Email email = modelMapper.map(emailDto, Email.class);
        email.setRequestAddress(WebUtil.getClientIpAddress(request));
        email.setRequestHeaders(WebUtil.getRequestHeaders(request));
        email.setRequestParams(WebUtil.getQueryParams(request));
        email.setCreated(Instant.now());

//        emailService.send(email);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/1")
    public ResponseEntity<Map<String, Object>> test1(HttpServletRequest request) {
        Email email = Email.builder()
                .from("admin@fmning.com")
                .to("synfm123@gmail.com")//noreply.fmning@gmail.com
                .subject("Monthly SIG refresh")
                .content(Instant.now().toString())
                .senderType(EmailSenderType.SEND_IN_BLUE)
                .created(Instant.now())
                .build();

        emailService.send(email);
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @GetMapping("/2")
    public ResponseEntity<Map<String, Object>> test2() {

        DataSource source = new FileDataSource("D:\\php\\glib-2.dll");
        log.info(source.getName());

        File f = new File("D:\\php\\glib-2.dll");
        log.warn(f.getName());

        return ResponseEntity.ok(Collections.emptyMap());
    }

}
