package com.fmning.tools.dto;

import com.fmning.tools.domain.RealEstate;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class RealEstateDto {
    String zid;
    String label;
    Date start;
    int balance;
    double rate;
    double monthly;
    List<RealEstate> history;
}