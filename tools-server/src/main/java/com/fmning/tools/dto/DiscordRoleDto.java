package com.fmning.tools.dto;

import com.fmning.tools.type.DiscordRoleType;
import lombok.RequiredArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@RequiredArgsConstructor
public class DiscordRoleDto {

    private final String id;
    private final String guildId;
    private final String name;
    private final String color;
    private final int position;
    private final Instant created;
    private final String ownerId;
    private final DiscordRoleType type;
    private String ownerName;

    public DiscordRoleDto withOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }
}