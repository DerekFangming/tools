package com.fmning.tools.service;

import com.fmning.tools.dto.ErrorDto;
import com.fmning.tools.util.WebUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//@EnableWebMvc
//@ControllerAdvice
public class ErrorHandler {

//    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorDto> handleException(Throwable ex, HttpServletRequest request) {

        Map<String, String> headers = WebUtil.getRequestHeaders(request);
        Map<String, String> cookies = WebUtil.getRequestCookies(request);
        Map<String, String> params = WebUtil.getQueryParams(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String body = null;
        try {
            body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
//            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
//            body = s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            System.out.println("IO Exception");
        }

        System.out.println("Caught error!");

        return ResponseEntity.ok(ErrorDto.builder().error("wtf").build());
    }
}
