package com.fmning.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.repository.ConfigRepo;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
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

    private String webhookUrl;

    private final ConfigRepo configRepo;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() throws JsonProcessingException {
        webhookUrl = configRepo.findById("HA_ALEXA_WEBHOOK_URL")
                .orElseThrow(() -> new IllegalStateException("Failed to get HA alexa webhook url")).getValue();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity alexaCode(@RequestBody AlexaCode alexaCode) {
        log.info("Received code from alexa: " + alexaCode.code);

        WebhookPayload payload = null;
        if (alexaCode.getCode() == 0) {
            payload = WebhookPayload.builder().command("goodnight").build();
        } else if (alexaCode.getCode() == 50) {
            payload = WebhookPayload.builder().command("1stAcOff").build();
        } else if (alexaCode.getCode() == 51) {
            payload = WebhookPayload.builder().command("1stAcCool").build();
        } else if (alexaCode.getCode() == 52) {
            payload = WebhookPayload.builder().command("1stAcHeat").build();
        } else if (alexaCode.getCode() >= 70 && alexaCode.getCode() <= 80) {
            payload = WebhookPayload.builder()
                    .command("1stAcTemp")
                    .temperature(alexaCode.code)
                    .build();
        } else if (alexaCode.getCode() == 100) {
            payload = WebhookPayload.builder().command("teslaAc").build();
        }

        if (payload != null) {
            postToHAWebhook(webhookUrl, payload);
        }

        return ResponseEntity.ok(Map.of());
    }

    private void postToHAWebhook(String url, WebhookPayload payload) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(okhttp3.RequestBody.create(objectMapper.writeValueAsString(payload), MediaType.parse("application/json; charset=utf-8")))
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

    @Data
    @Builder
    static class WebhookPayload {
        String command;
        int temperature;
    }

}
