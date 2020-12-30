package com.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="guild_id")
    private long guildId;

    @Column(name="apex_id")
    private String apexId;

    @Column(name="birthday")
    private String birthday;
}
