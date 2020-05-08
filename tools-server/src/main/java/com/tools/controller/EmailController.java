package com.tools.controller;

import com.tools.domain.Email;
import com.tools.dto.EmailDto;
import com.tools.repository.EmailRepo;
import com.tools.service.EmailService;
import com.tools.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping(value = "/api/email")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class EmailController {

    private final EmailRepo emailRepo;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<Void> post(@RequestBody EmailDto emailDto, HttpServletRequest request) {

        Email email = modelMapper.map(emailDto, Email.class);
        email.setRequestAddress(WebUtil.getClientIpAddress(request));
        email.setRequestHeaders(WebUtil.getRequestHeaders(request));
        email.setRequestParams(WebUtil.getQueryParams(request));
        email.setCreated(Instant.now());

        emailService.send(email);

        log.info("Error");

        System.out.println(request.getRemoteAddr());

//        Map<String, String> headers = WebUtil.getRequestHeaders(request);
//
//        System.out.println(headers);
//
//        Map<String, String> cookies = WebUtil.getRequestCookies(request);
//        System.out.println(cookies);


//        Map<String, Object> map = new HashMap<>();
//        map.put("key", "value");
//        Email email = Email.builder().content(map).build();
//        emailRepo.save(email);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/1")
    public ResponseEntity<Map<String, Object>> test1(HttpServletRequest request) {
        Email email = new Email();


        email.setRequestAddress(WebUtil.getClientIpAddress(request));
        email.setRequestHeaders(WebUtil.getRequestHeaders(request));
        email.setRequestParams(WebUtil.getQueryParams(request));
        email.setCreated(Instant.now());

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
