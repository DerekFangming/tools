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
@Table(name="tl_discord_users")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordUser {

    @Id
    @Column(name="id")
    private String id;

    @Column(name="name")
    private String name;

    @Column(name="guild_id")
    private String guildId;

    @Column(name="nickname")
    private String nickname;

    @Column(name="avatar_id")
    private String avatarId;

    @Column(name="roles")
    private String roles;

    @Column(name="created_date")
    private Instant createdDate;

    @Column(name="joined_date")
    private Instant joinedDate;

    @Column(name="boosted_date")
    private Instant boostedDate;

    @Column(name="apex_id")
    private String apexId;

    @Column(name="birthday")
    private String birthday;

    @Column(name="boost_channel_id")
    private String boostChannelId;

    @Column(name="temp_channel_id")
    private String tempChannelId;

    @Column(name="voice_minutes")
    private int voiceMinutes;

    @Column(name="voice_last_join")
    private Instant voiceLastJoin;

    @Column(name="lottery_chance")
    private int lotteryChance;
}