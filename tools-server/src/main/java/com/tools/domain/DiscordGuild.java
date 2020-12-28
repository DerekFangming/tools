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
    private String id;

    @Column(name="name")
    private String name;

    @Column(name="welcome_enabled")
    private boolean welcomeEnabled;

    @Column(name="welcome_title")
    private String title;

    @Column(name="welcome_description")
    private String description;

    @Column(name="welcome_thumbnail")
    private String thumbnail;

    @Column(name="welcome_footer")
    private String footer;

    @Column(name="welcome_color")
    private String color;

    @Column(name="welcome_channel_id")
    private String channelId;

    @Column(name="welcome_role_id")
    private String roleId;
}
