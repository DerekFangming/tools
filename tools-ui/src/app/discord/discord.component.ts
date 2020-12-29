import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { DiscordGuildConfig } from '../model/discord-guild-config';
import { DiscordObject } from '../model/discord-object';
import { DiscordUserLog } from '../model/discord-user-log';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord',
  templateUrl: './discord.component.html',
  styleUrls: ['./discord.component.css']
})
export class DiscordComponent implements OnInit {

  tab = 'logs';
  loadingUserLogs = false;
  loadingBotConfig = false;
  loadingChannels = false;
  loadingRoles = false;

  userLogList: DiscordUserLog[];
  guildConfig: DiscordGuildConfig;
  guildRoleList: DiscordObject[];
  guildChannelList: DiscordObject[];

  selectedChannelName = '';
  selectedRoleName = '';

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService, private activatedRoute: ActivatedRoute, private router: Router) {
    this.title.setTitle('Discord Insights');
    let tab = this.activatedRoute.snapshot.queryParamMap.get('tab');
    this.tab = tab == null ? 'logs' : tab;
  }

  ngOnInit() {
    this.onTabSelected(this.tab);
  }

  onTabSelected(newTab: string) {
    this.tab = newTab;
    if (this.tab == 'logs') {
      this.loadUserLogs();
    } else {
      this.loadBotConfig();
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: { tab: newTab },
      queryParamsHandling: 'merge'
    });
  }

  loadUserLogs() {
    this.loadingUserLogs = true;
    this.http.get<DiscordUserLog[]>(environment.urlPrefix + 'api/discord/default/user-logs').subscribe(userLogList => {
      this.userLogList = userLogList;
      this.loadingUserLogs = false;
    }, error => {
      this.loadingUserLogs = false;
      console.log(error.error);
    });
  }

  loadBotConfig() {
    this.loadingBotConfig = true;
    this.loadingChannels = true;
    this.loadingRoles = true;

    this.http.get<DiscordGuildConfig>(environment.urlPrefix + 'api/discord/default/config').subscribe(guildConfig => {
      this.guildConfig = guildConfig;
      this.processSelectedDropdowns();
      this.loadingBotConfig = false;
    }, error => {
      this.loadingBotConfig = false;
      console.log(error.error);
    });

    this.http.get<DiscordObject[]>(environment.urlPrefix + 'api/discord/default/channels').subscribe(guildChannelList => {
      this.guildChannelList = guildChannelList;
      this.processSelectedDropdowns();
      this.loadingChannels = false;
    }, error => {
      this.loadingChannels = false;
      console.log(error.error);
    });

    this.http.get<DiscordObject[]>(environment.urlPrefix + 'api/discord/default/roles').subscribe(guildRoleList => {
      this.guildRoleList = guildRoleList;
      this.processSelectedDropdowns();
      this.loadingRoles = false;
    }, error => {
      this.loadingRoles = false;
      console.log(error.error);
    });
  }

  processSelectedDropdowns() {
    if (this.guildConfig != null && this.guildChannelList != null && this.guildRoleList != null) {
      this.guildChannelList.unshift(new DiscordObject({id: null, name: 'Disable channel message'}))
      this.guildRoleList.unshift(new DiscordObject({id: null, name: 'Disable role assignment'}))

      if (this.guildConfig.welcomeChannelId == null) {
        this.selectedChannelName = this.guildChannelList[0].name;
      } else {
        this.selectedChannelName = this.guildChannelList.find(c => c.id == this.guildConfig.welcomeChannelId).name;
      }

      if (this.guildConfig.welcomeRoleId == null) {
        this.selectedRoleName = this.guildRoleList[0].name;
      } else {
        this.selectedRoleName = this.guildRoleList.find(r => r.id == this.guildConfig.welcomeRoleId).name;
      }
    }
  }

  onChannelSelected(option: DiscordObject) {
    this.selectedChannelName = option.name;
    this.guildConfig.welcomeChannelId = option.id;
  }

  onRoleSelected(option: DiscordObject) {
    this.selectedRoleName = option.name;
    this.guildConfig.welcomeRoleId = option.id;
  }

  onSaveCahnges() {
    console.log(this.guildConfig);
  }

}
