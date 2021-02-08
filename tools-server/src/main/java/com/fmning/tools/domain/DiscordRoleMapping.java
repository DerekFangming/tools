package com.fmning.tools.domain;

import com.fmning.tools.type.DiscordRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_discord_role_mappings")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordRoleMapping {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="role_id")
    private String roleId;

    @Column(name="enabled")
    private boolean enabled;

    @Column(name="code")
    private String code;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private DiscordRoleType type;

    @Column(name="owner_id")
    private String ownerId;

    @Column(name="approver_id")
    private String approverId;

    @Column(name="created")
    private Instant created;
}
