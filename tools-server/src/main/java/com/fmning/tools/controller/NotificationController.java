package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.repository.ConfigRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping(value = "/api/notifications")
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class NotificationController {
    private String voiceMonkeyUrl;
    private String pushoverUrl;

    private final ConfigRepo configRepo;
    private final OkHttpClient client;
    private final ToolsProperties toolsProperties;

    @PostConstruct
    public void init() {
        voiceMonkeyUrl = configRepo.findById("VOICE_MONKEY_URL")
                .orElseThrow(() -> new IllegalStateException("Failed to get voice monkey url")).getValue();

        pushoverUrl = configRepo.findById("PUSHOVER_URL")
                .orElseThrow(() -> new IllegalStateException("Failed to get pushover url")).getValue();
    }

    @GetMapping()
    public void all(@RequestParam("message") String message) {
        if (StringUtils.isNotEmpty(message)) {
            alexaNotification(message);
            iftttNotification(message);
        }
    }

    @PostMapping()
    public void all(@org.springframework.web.bind.annotation.RequestBody NotificationPayload payload) {
        if (StringUtils.isNotEmpty(payload.getMessage())) {
            alexaNotification(payload.getMessage());
            iftttNotification(payload.getMessage());
        }
    }

    @GetMapping("/ifttt")
    public void ifttt(@RequestParam("message") String message) {
        if (StringUtils.isNotEmpty(message)) {
            iftttNotification(message);
        }
    }

    @PostMapping("/ifttt")
    public void ifttt(@org.springframework.web.bind.annotation.RequestBody NotificationPayload payload) {
        if (StringUtils.isNotEmpty(payload.getMessage())) {
            iftttNotification(payload.getMessage());
        }
    }

    private void iftttNotification(String message) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("value1", message);

            Request request = new Request.Builder()
                    .url("https://maker.ifttt.com/trigger/notification/with/key/" + toolsProperties.getIftttKey())
                    .post(okhttp3.RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Call call = client.newCall(request);
            call.execute();
        } catch (Exception e) {
            log.error("Failed to send IFTTT notification", e);
        }
    }

    @GetMapping("/alexa")
    public void alexa(@RequestParam("message") String message) {
        if (StringUtils.isNotEmpty(message)) {
            alexaNotification(message);
        }
    }

    @PostMapping("/alexa")
    public void alexa(@org.springframework.web.bind.annotation.RequestBody NotificationPayload payload) {
        if (StringUtils.isNotEmpty(payload.getMessage())) {
            alexaNotification(payload.getMessage());
        }
    }

    private void alexaNotification(String message) {
        try {
            Request request = new Request.Builder()
                    .url(voiceMonkeyUrl + "&announcement=" + message)
                    .get()
                    .build();

            Call call = client.newCall(request);
            Response res = call.execute();
        } catch (Exception e) {
            log.error("Failed to send Alexa notification", e);
        }
    }

    @GetMapping("/pushover")
    public void pushover(@RequestParam("message") String message,
                         @RequestParam(value="title", required=false) String title,
                         @RequestParam(value="device", required=false) String device,
                         @RequestParam(value="priority", required=false) String priority) {
        if (StringUtils.isNotEmpty(message)) {
            pushoverNotification(message, title, device, priority);
        }
    }
    @PostMapping("/pushover")
    public void pushover(@org.springframework.web.bind.annotation.RequestBody NotificationPayload payload) {
        if (StringUtils.isNotEmpty(payload.getMessage())) {
            pushoverNotification(payload.getMessage(), payload.getTitle(), payload.getDevice(), payload.getPriority());
        }
    }

    private void pushoverNotification(String message, String title, String device, String priority) {
        try {
            String url = pushoverUrl + "&message=" + message;
            if (StringUtils.isNotEmpty(title)) url += "&title=" + title;
            if (StringUtils.isNotEmpty(priority)) url += "&priority=2&retry=30&expire=300";
            url += device == null ? "&device=nfmiphone" : "&device=" + device;
            url += "&sound=pushover";

            Request request = new Request.Builder()
                    .url(url)
                    .post(okhttp3.RequestBody.create("{}", MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Call call = client.newCall(request);
            Response res = call.execute();
        } catch (Exception e) {
            log.error("Failed to send IFTTT notification", e);
        }
    }

    @Data
    private static class NotificationPayload {
        String message;
        String title;
        String device;
        String priority;
    }

}
