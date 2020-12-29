import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';
import { DiscordGuildConfig } from '../model/discord-guild-config';
import { DiscordObjectDto } from '../model/discord-object';
import { DiscordUserLog } from '../model/discord-user-log';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord',
  templateUrl: './discord.component.html',
  styleUrls: ['./discord.component.css']
})
export class DiscordComponent implements OnInit {

  tab = 'logs';
  loadingUserLogs = true;
  loadingBotConfig = false;
  loadingChannels = false;
  loadingRoles = false;

  userLogList: DiscordUserLog[];
  guildConfig: DiscordGuildConfig;
  guildRoleList: DiscordObjectDto[];
  guildChannelList: DiscordObjectDto[];

  constructor(private http: HttpClient, private title: Title, private utils: UtilsService) {
    this.title.setTitle('Discord Insights');
  }

  ngOnInit() {
    this.loadUserLogs();
  }

  onTabSelected(newTab: string) {
    this.tab = newTab;
    if (this.tab == 'logs') {
      this.loadUserLogs();
    } else {
      this.loadBotConfig();
    }
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
      this.loadingBotConfig = false;
    }, error => {
      this.loadingBotConfig = false;
      console.log(error.error);
    });

    this.http.get<DiscordObjectDto[]>(environment.urlPrefix + 'api/discord/default/channels').subscribe(guildChannelList => {
      this.guildChannelList = guildChannelList;
      this.loadingChannels = false;
    }, error => {
      this.loadingChannels = false;
      console.log(error.error);
    });

    this.http.get<DiscordObjectDto[]>(environment.urlPrefix + 'api/discord/default/roles').subscribe(guildRoleList => {
      this.guildRoleList = guildRoleList;
      this.loadingRoles = false;
    }, error => {
      this.loadingRoles = false;
      console.log(error.error);
    });
  }

}
