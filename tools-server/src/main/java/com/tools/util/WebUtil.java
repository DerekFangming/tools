package com.tools.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class WebUtil {

    public static Map<String, String> getRequestHeaders(HttpServletRequest request) {
        return Collections
                .list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));
    }

    public static Map<String, String> getRequestCookies(HttpServletRequest request) {
        return Arrays.stream(request.getCookies()).collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

    public static Map<String, String> getQueryParams(HttpServletRequest request) {
        return Collections.list(request.getParameterNames())
                .stream()
                .collect(Collectors.toMap(parameterName -> parameterName, parameterName -> String.join(",", request.getParameterValues(parameterName))));
    }
}
