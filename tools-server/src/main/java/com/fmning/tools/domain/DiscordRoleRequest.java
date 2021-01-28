package com.fmning.tools.domain;

import com.fmning.tools.type.DiscordRoleRequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_discord_role_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordRoleRequest {

    @Id
    @Column(name="id")
    private String id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="role_id")
    private String roleId;

    @Column(name="action")
    @Enumerated(EnumType.STRING)
    private DiscordRoleRequestType action;

    @Column(name="requester_id")
    private String requesterId;

    @Column(name="approver_id")
    private String approverId;

    @Column(name="created")
    private Instant created;
}
