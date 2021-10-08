package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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