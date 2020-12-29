package com.tools.domain;

import com.tools.type.DiscordUserLogActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tl_discord_user_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordUserLog {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name="guild_id")
    private long guildId;

    @Column(name="user_id")
    private long userId;

    @Column(name="name")
    private String name;

    @Column(name="action")
    @Enumerated(EnumType.STRING)
    private DiscordUserLogActionType action;

    @Column(name="created")
    private Instant created;
}