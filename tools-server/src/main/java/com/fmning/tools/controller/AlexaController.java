package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.Image;
import com.fmning.tools.repository.ConfigRepo;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CommonsLog
@RestController
@RequestMapping(value = "/api/alexa")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class AlexaController {

    private String temperatureWebhookUrl;

    private final ConfigRepo configRepo;
    private final OkHttpClient client;

    @PostConstruct
    public void init() {
        temperatureWebhookUrl = configRepo.findById("TEMPERATURE_WEBHOOK_URL")
                .orElseThrow(() -> new IllegalStateException("Failed to get temperature webhook url")).getValue();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity alexaCode(@RequestBody AlexaCode alexaCode) {
        log.info("Received code from alexa: " + alexaCode.code);

        if (alexaCode.getCode() >= 70 && alexaCode.getCode() <= 80) {
            JSONObject payload = new JSONObject().put("temperature", alexaCode.code);
            postToHAWebhook(temperatureWebhookUrl, payload);
        }

        return ResponseEntity.ok(Map.of());
    }

    private void postToHAWebhook(String url, JSONObject payload) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(okhttp3.RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Call call = client.newCall(request);
            call.execute();
        } catch (Exception e) {
            log.error("Failed to call webhook", e);
        }
    }

    @Data
    static class AlexaCode {
        int code;
    }

}
