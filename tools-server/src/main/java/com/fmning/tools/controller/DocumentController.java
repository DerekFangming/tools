package com.fmning.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.domain.*;
import com.fmning.tools.dto.DocumentDto;
import com.fmning.tools.dto.RealEstateDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.service.RealEstateService;
import com.fmning.tools.type.ImageType;
import jakarta.persistence.Column;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.core.codec.ResourceEncoder.DEFAULT_BUFFER_SIZE;

@CommonsLog
@RestController
@RequestMapping(value = "/api/documents")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DocumentController {

    private final ObjectMapper objectMapper;

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

    private DocumentDto domainToDto(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .name(document.getName())
                .owner(document.getOwner())
                .expirationDate(document.getExpirationDate())
                .images(Arrays.asList(document.getImages().split(",")))
                .build();
    }
//
//    @RequestMapping(value = "/spending/accounts", method = RequestMethod.POST)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public SpendingAccount createAccount(@RequestBody SpendingAccount account) {
//        account.setId(0);
//        return accountRepo.save(account);
//    }
//
//    @RequestMapping(value = "/spending/accounts/{id}", method = RequestMethod.PUT)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public SpendingAccount updateAccount(@PathVariable int id, @RequestBody SpendingAccount account) {
//        SpendingAccount spendingAccount = accountRepo.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Not found"));
//        spendingAccount.setName(account.getName());
//        spendingAccount.setIdentifier(account.getIdentifier());
//        spendingAccount.setIcon(account.getIcon());
//        spendingAccount.setOwner(account.getOwner());
//        return accountRepo.save(spendingAccount);
//    }
//
//    @RequestMapping(value = "/spending/accounts/{id}", method = RequestMethod.DELETE)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public void deleteAccount(@PathVariable int id) {
//        accountRepo.deleteById(id);
//    }
//
//    @RequestMapping(value = "/spending/transactions", method = RequestMethod.GET)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public List<SpendingTransaction> getTransactions(@RequestParam(required=false) Date from,
//                                                     @RequestParam(required=false) Date to) {
//        if (from == null) {
//            return transactionRepo.findAll();
//        }
//        from.setTime(from.getTime() - 24 * 60 * 60 * 1000);
//        if (to == null) {
//            return transactionRepo.findAllByDateAfter(from);
//        } else {
//            return transactionRepo.findAllByDateAfterAndDateBefore(from, to);
//        }
//    }
//
//
//
//    @RequestMapping(value = "/real-estates", method = RequestMethod.GET)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public List<RealEstateDto> listRealEstates() throws JsonProcessingException {
//
//        String realEstate = configRepo.findById("REAL_ESTATE")
//                .orElseThrow(() -> new IllegalStateException("Failed to get real estate")).getValue();
//
//
//        List<RealEstateDto> realEstates = objectMapper.readValue(realEstate, new TypeReference<>() {});
//
//        for (RealEstateDto r : realEstates) {
//            List<RealEstate> history = realEstateRepo.findTop12ByZidOrderByDateDesc(r.getZid());
//            Collections.reverse(history);
//            r.setHistory(history);
//        }
//
//        return realEstates;
//    }
//
//    @RequestMapping(value = "/reload-real-estates", method = RequestMethod.GET)
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'FIN')")
//    public void reloadRealEstates() {
//        try {
//            realEstateService.processCurrentMonth();
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Failed to reload", e);
//        }
//    }

}
