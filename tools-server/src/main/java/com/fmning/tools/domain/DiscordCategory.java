package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="tl_discord_categories")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordCategory {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="name")
    private String name;

    @Column(name="position")
    private int position;

    @Column(name="created")
    private Instant created;

    @Transient
    private List<DiscordChannel> channels;
}
