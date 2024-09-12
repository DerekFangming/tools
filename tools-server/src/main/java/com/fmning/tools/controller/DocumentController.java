package com.fmning.tools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.domain.*;
import com.fmning.tools.dto.DocumentDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.type.ImageType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.core.codec.ResourceEncoder.DEFAULT_BUFFER_SIZE;

@CommonsLog
@RestController
@RequestMapping(value = "/api/documents")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DocumentController {

    private final ImageRepo imageRepo;
    private final DocumentRepo documentRepo;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public DocumentDto create(@RequestBody DocumentDto dto) throws MalformedURLException {
        Document document = Document.builder()
                .name(dto.getName())
                .owner(dto.getOwner())
                .expirationDate(dto.getExpirationDate())
                .build();

        List<String> imageList = dto.getImages().stream().map(i -> {
            if (StringUtils.isNumeric(i)) return i;

            Image image = Image.builder()
                    .type(ImageType.DOCUMENT)
                    .data(i.split(",")[1])
                    .created(Instant.now())
                    .build();

            imageRepo.save(image);

            return "" + image.getId();
        }).collect(Collectors.toList());

        document.setImages(String.join(",", imageList));
        documentRepo.save(document);

        return domainToDto(document);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public DocumentDto update(@PathVariable int id, @RequestBody DocumentDto dto) throws MalformedURLException {
        Document document = documentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        document.setName(dto.getName());
        document.setOwner(dto.getOwner());
        document.setExpirationDate(dto.getExpirationDate());

        Set<String> existing = new HashSet<>(Arrays.asList(document.getImages().split(",")));
        Set<String> incoming = new HashSet<>(dto.getImages());

        // Delete images if not included from the updated document
        for (String imgId : existing) {
            if (!incoming.contains(imgId)) {
                imageRepo.deleteById(Integer.parseInt(imgId));
            }
        }

        // Save new images from the updated document
        List<String> imageList = dto.getImages().stream().map(i -> {
            if (StringUtils.isNumeric(i)) return i;

            Image image = Image.builder()
                    .type(ImageType.DOCUMENT)
                    .data(i.split(",")[1])
                    .created(Instant.now())
                    .build();

            imageRepo.save(image);

            return "" + image.getId();
        }).collect(Collectors.toList());

        document.setImages(String.join(",", imageList));
        documentRepo.save(document);

        return domainToDto(document);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public List<DocumentDto> getDocuments() {
        return documentRepo.findAll().stream().map(this::domainToDto).collect(Collectors.toList());
    }

    @RequestMapping(value = "/images/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public void getDocumentImage(@PathVariable int id, HttpServletResponse response) {
        Image image = imageRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Image ID " + id + " not found"));
        if (image.getType() != ImageType.DOCUMENT) throw new IllegalArgumentException("Image ID " + id + " is not the right type");


        byte[] file = Base64.decodeBase64(image.getData());
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setContentType("image/png");
        response.addHeader("Cache-Control", "max-age=30692876");
        try {
            response.getOutputStream().write(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write data", e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public void delete(@PathVariable int id) {
        Document document = documentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (document.getImages() != null) {
            String[] images = document.getImages().split(",");
            for (String img : images) {
                imageRepo.deleteById(Integer.parseInt(img));
            }
        }
        documentRepo.delete(document);
    }

    private DocumentDto domainToDto(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .name(document.getName())
                .owner(document.getOwner())
                .expirationDate(document.getExpirationDate())
                .images(Arrays.asList(document.getImages().split(",")))
                .build();
    }

}
