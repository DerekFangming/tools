package com.tools.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class LoginController {

    @GetMapping("/login-redirect")
    public void loginRedirect(@RequestParam("goto") String gotoUrl, HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader(HttpHeaders.LOCATION, gotoUrl);
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    private String inMem = "null";

    @GetMapping("/ping1")
    public String ping1() {
        return inMem;
    }

    @GetMapping("/ping1/{message}")
    public void ping1(@PathVariable("message") String message) {
        inMem = message;
    }

}
