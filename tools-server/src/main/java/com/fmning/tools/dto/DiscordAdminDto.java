package com.fmning.tools.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordAdminDto {
    private String memberId;
    private String roleId;
}
