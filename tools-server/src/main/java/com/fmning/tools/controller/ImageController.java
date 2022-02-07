package com.fmning.tools.controller;

import com.fmning.tools.ToolsProperties;
import com.fmning.tools.domain.Image;
import com.fmning.tools.repository.ImageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/images")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class ImageController {

    private final ImageRepo imageRepo;
    private final ToolsProperties toolsProperties;
    private final OkHttpClient client;

    @GetMapping
    public ResponseEntity<List<Image>> getImages(){
        return ResponseEntity.ok(imageRepo.findAllByOrderByIdDesc());
    }

    @PostMapping
    public ResponseEntity<Image> create(@RequestBody Image image) {
        imageRepo.save(processImage(image));
        return ResponseEntity.ok(image);
    }

    @PostMapping
    @RequestMapping(value = "/bulk")
    public ResponseEntity<List<Image>> bulkCreate(@RequestBody List<Image> images) {
        images = images.stream()
                .map(this::processImage)
                .collect(Collectors.toList());

        imageRepo.saveAll(images);
        return ResponseEntity.ok(images);
    }

    private Image processImage(Image image) {
        image.setId(0);
        image.setCreated(Instant.now());

        if (image.getData() != null) {
            String[] parts = image.getData().split(",");
            String data = parts.length == 2 ? parts[1] : parts[0];
            try {
                JSONObject payload = new JSONObject();
                payload.put("image", data);

                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image")
                        .post(okhttp3.RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8")))
                        .addHeader("authorization", "Client-ID " + toolsProperties.getImgurClientId())
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
                String link = json.getJSONObject("data").getString("link");
                image.setUrl(link);
            } catch (Exception e) {
                image.setUrl(null);
                log.error("Failed to upload images", e);
                e.printStackTrace();
            }
        }

        image.setData(null);
        return image;
    }

}
