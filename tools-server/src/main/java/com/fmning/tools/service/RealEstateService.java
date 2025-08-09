package com.fmning.tools.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.domain.RealEstate;
import com.fmning.tools.dto.RealEstateDto;
import com.fmning.tools.repository.ConfigRepo;
import com.fmning.tools.repository.RealEstatePK;
import com.fmning.tools.repository.RealEstateRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@CommonsLog
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RealEstateService {

    private String zillowApiKey;
    private Object currentValue;
    private Instant currentValueLastCheck;

    private final ConfigRepo configRepo;
    private final RealEstateRepo realEstateRepo;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    @PostConstruct
    public void init() {
        zillowApiKey = configRepo.findById("ZILLOW_API_KEY")
                .orElseThrow(() -> new IllegalStateException("Failed to get zillow API key")).getValue();
    }

    @Scheduled(cron = "0 0 14 1 * ?")// 2 pm, 1st day of every month
    public void processCurrentMonth() throws JsonProcessingException {
        String realEstateStr = configRepo.findById("REAL_ESTATE")
                .orElseThrow(() -> new IllegalStateException("Failed to get real estate")).getValue();
        List<RealEstateDto> realEstateDtos = objectMapper.readValue(realEstateStr, new TypeReference<>() {});

        for (RealEstateDto r : realEstateDtos) {
            // Calculate remaining balance
            Date firstDateOfCurrentMonth = Date.valueOf(LocalDate.now().withDayOfMonth(1));
            long monthsBetween = ChronoUnit.MONTHS.between(
                    LocalDate.parse(r.getStart().toString()),
                    LocalDate.parse(firstDateOfCurrentMonth.toString()));

            double balance = r.getBalance();
            while (monthsBetween > 0) {
                monthsBetween --;
                double interest = (balance * r.getRate()) / 12;
                double principal = r.getMonthly() - interest;
                balance = balance - principal;
            }

            // Get estimate from zillow
            int zestimate = 0;
            try {
                Request request = new Request.Builder()
                        .url(HttpUrl
                                .parse("https://api.bridgedataoutput.com/api/v2/zestimates_v2/zestimates")
                                .newBuilder()
                                .addQueryParameter("access_token", zillowApiKey)
                                .addQueryParameter("zpid", r.getZid())
                                .build())
                        .get()
                        .build();

                Call call = client.newCall(request);
                Response res = call.execute();

                JSONObject obj = new JSONObject(res.body().string());
                zestimate = obj.getJSONArray("bundle").getJSONObject(0).getNumber("zestimate").intValue();
            } catch (Exception e) {
                log.error("Failed to get zestimate", e);
            }

            // Save records
            RealEstate realEstate = RealEstate.builder()
                    .zid(r.getZid())
                    .date(firstDateOfCurrentMonth)
                    .value(zestimate)
                    .balance((int)balance)
                    .build();

            realEstateRepo.save(realEstate);
        }
    }

    public Object getSummary() throws JsonProcessingException {
        if (currentValueLastCheck != null && ChronoUnit.HOURS.between(currentValueLastCheck, Instant.now()) < 72) {
            return currentValue;
        }

        // Reload
        String realEstate = configRepo.findById("REAL_ESTATE")
                .orElseThrow(() -> new IllegalStateException("Failed to get real estate")).getValue();

        List<RealEstateDto> realEstates = objectMapper.readValue(realEstate, new TypeReference<>() {});
        Date firstDateOfCurrentMonth = Date.valueOf(LocalDate.now().withDayOfMonth(1));

        int value = 0, balance = 0;
        for (RealEstateDto r : realEstates) {
            RealEstate re = realEstateRepo.findById(new RealEstatePK(r.getZid(), firstDateOfCurrentMonth)).orElseGet(() -> {

                try {
                    processCurrentMonth();
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Failed to get current month value", e);
                }

                return realEstateRepo.findById(new RealEstatePK(r.getZid(), firstDateOfCurrentMonth))
                        .orElseThrow(() -> new IllegalStateException("Failed to get current month value after retry"));
            });
            balance += re.getBalance();

            try {
                Request request = new Request.Builder()
                        .url(HttpUrl
                                .parse("https://api.bridgedataoutput.com/api/v2/zestimates_v2/zestimates")
                                .newBuilder()
                                .addQueryParameter("access_token", zillowApiKey)
                                .addQueryParameter("zpid", r.getZid())
                                .build())
                        .get()
                        .build();

                Call call = client.newCall(request);
                Response res = call.execute();

                JSONObject obj = new JSONObject(res.body().string());
                value += obj.getJSONArray("bundle").getJSONObject(0).getNumber("zestimate").intValue();
            } catch (Exception e) {
                log.error("Failed to get zestimate", e);
            }
        }

        currentValueLastCheck = Instant.now();
        currentValue = Map.of("value", value, "balance", balance, "equity", value - balance);
        return currentValue;
    }

}
