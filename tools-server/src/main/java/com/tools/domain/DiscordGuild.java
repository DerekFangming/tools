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
@Table(name="tl_discord_guilds")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordGuild {
    
    @Id
    @Column(name="id")
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="welcome_enabled")
    private boolean welcomeEnabled;

    @Column(name="welcome_setting")
    private String welcomeSetting;
}
