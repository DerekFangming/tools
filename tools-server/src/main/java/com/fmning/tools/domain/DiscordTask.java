package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_discord_tasks")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordTask {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private String id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="type")
    private String type;

    @Column(name="payload")
    private String payload;

    @Column(name="timeout")
    private Instant timeout;

    @Column(name="created")
    private Instant created;

}