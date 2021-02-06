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
@Table(name="tl_discord_roles")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordRole {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="name")
    private String name;

    @Column(name="color")
    private String color;

    @Column(name="position")
    private int position;

    @Column(name="created")
    private Instant created;

    @Column(name="owner_id")
    private String ownerId;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private DiscordRoleType type;

    @Transient
    private String ownerName;

    public DiscordRole withOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }
}
