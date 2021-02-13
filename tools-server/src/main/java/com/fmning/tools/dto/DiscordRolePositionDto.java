package com.fmning.tools.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Data
@RequiredArgsConstructor
public class DiscordRolePositionDto {
    private final String roleId;
    private final String ownerName;
    private final Instant ownerBoostTime;
    private String roleName;
    private int position;

    public DiscordRolePositionDto withRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public DiscordRolePositionDto withRolePosition(int position) {
        this.position = position;
        return this;
    }
}
