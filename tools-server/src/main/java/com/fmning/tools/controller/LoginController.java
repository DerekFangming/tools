package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class LoginController {

    @Autowired
    ToolsProperties toolsProperties;

    @GetMapping("/login-redirect")
    public void loginRedirect(@RequestParam("goto") String gotoUrl, HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader(HttpHeaders.LOCATION, gotoUrl);
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
