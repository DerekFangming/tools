package com.fmning.tools.domain;

import com.fmning.tools.type.DiscordTaskType;
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
    private int id;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private DiscordTaskType type;

    @Column(name="payload")
    private String payload;

    @Column(name="timeout")
    private Instant timeout;

    @Column(name="created")
    private Instant created;

}