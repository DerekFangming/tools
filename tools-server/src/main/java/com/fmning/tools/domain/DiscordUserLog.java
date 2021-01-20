package com.fmning.tools.domain;

import com.fmning.tools.type.DiscordUserLogActionType;
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
    private String guildId;

    @Column(name="user_id")
    private String userId;

    @Column(name="name")
    private String name;

    @Column(name="nickname")
    private String nickname;

    @Column(name="action")
    @Enumerated(EnumType.STRING)
    private DiscordUserLogActionType action;

    @Column(name="created")
    private Instant created;
}
