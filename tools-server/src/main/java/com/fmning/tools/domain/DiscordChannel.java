package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.ChannelType;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_discord_channels")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordChannel {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="category_id")
    private String categoryId;

    @Column(name="name")
    private String name;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @Column(name="position")
    private int position;

    @Column(name="created")
    private Instant created;
}
