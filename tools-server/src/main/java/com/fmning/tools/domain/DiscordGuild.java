package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

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
    private String welcomeTitle;

    @Column(name="welcome_description")
    private String welcomeDescription;

    @Column(name="welcome_thumbnail")
    private String welcomeThumbnail;

    @Column(name="welcome_footer")
    private String welcomeFooter;

    @Column(name="welcome_color")
    private String welcomeColor;

    @Column(name="welcome_channel_id")
    private String welcomeChannelId;

    @Column(name="welcome_role_id")
    private String welcomeRoleId;

    @Column(name="debug_channel_id")
    private String debugChannelId;

    @Column(name="birthday_enabled")
    private boolean birthdayEnabled;

    @Column(name="birthday_message")
    private String birthdayMessage;

    @Column(name="birthday_role_id")
    private String birthdayRoleId;

    @Column(name="birthday_channel_id")
    private String birthdayChannelId;

    @Column(name="role_enabled")
    private boolean roleEnabled;

    @Column(name="role_level_requirement")
    private int roleLevelRequirement;

    @Column(name="role_name_blacklist")
    private String roleNameBlacklist;

    @Column(name="role_color_blacklist")
    private String roleColorBlacklist;

    @Column(name="role_level_rank_role_id")
    private String roleLevelRankRoleId;

    @Column(name="role_boost_rank_role_id")
    private String roleBoostRankRoleId;

    @Column(name="channel_enabled")
    private boolean channelEnabled;

    @Column(name="channel_boost_cat_id")
    private String channelBoostCatId;

    @Column(name="channel_temp_cat_id")
    private String channelTempCatId;

}
