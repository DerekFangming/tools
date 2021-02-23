import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NotifierService } from 'angular-notifier';
import { environment } from 'src/environments/environment';
import { DiscordGuildConfig } from '../model/discord-guild-config';
import { DiscordObject } from '../model/discord-object';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-config',
  templateUrl: './discord-config.component.html',
  styleUrls: ['./discord-config.component.css']
})
export class DiscordConfigComponent implements OnInit {

  loadingBotConfig = false;
  loadingChannels = false;
  loadingRoles = false;
  updatingConfig = false;

  guildConfig: DiscordGuildConfig;
  guildRoleList: DiscordObject[];
  guildChannelList: DiscordObject[];
  roleNameBlacklist: String[];
  roleColorBlacklist: String[];

  selectedWelcomeChannelName = '';
  selectedBirthdayChannelName = '';
  
  selectedWelcomeRoleName = '';
  selectedBirthdayRoleName = '';
  selectedLevelRankRoleName = '';
  selectedBoostRankRoleName = '';

  forbiddenRoleName = '';
  forbiddenRoleColor = '';

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService, private notifierService: NotifierService) {
    this.title.setTitle('Discord Configurations');
  }

  ngOnInit() {
    this.loadBotConfig();
  }

  loadBotConfig() {
    this.loadingBotConfig = true;
    this.loadingChannels = true;
    this.loadingRoles = true;
    this.guildConfig = null;
    this.guildChannelList = null;
    this.guildRoleList = null;

    this.http.get<DiscordGuildConfig>(environment.urlPrefix + 'api/discord/default/config').subscribe(guildConfig => {
      this.guildConfig = guildConfig;
      this.processSelectedDropdowns();

      this.roleNameBlacklist = guildConfig.roleNameBlacklist.split(/,+/);
      this.roleColorBlacklist = guildConfig.roleColorBlacklist.split(/,+/);
      this.loadingBotConfig = false;
    }, error => {
      this.loadingBotConfig = false;
      console.log(error.error);
    });

    this.http.get<DiscordObject[]>(environment.urlPrefix + 'api/discord/default/text-channels').subscribe(guildChannelList => {
      this.guildChannelList = guildChannelList;
      this.processSelectedDropdowns();
      this.loadingChannels = false;
    }, error => {
      this.loadingChannels = false;
      console.log(error.error);
    });

    this.http.get<DiscordObject[]>(environment.urlPrefix + 'api/discord/default/role-configs').subscribe(guildRoleList => {
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

      this.selectedWelcomeChannelName = this.getChannelNameFromList(this.guildConfig.welcomeChannelId);
      this.selectedBirthdayChannelName = this.getChannelNameFromList(this.guildConfig.birthdayChannelId);
      this.selectedWelcomeRoleName = this.getRoleNameFromList(this.guildConfig.welcomeRoleId);
      this.selectedBirthdayRoleName = this.getRoleNameFromList(this.guildConfig.birthdayRoleId);
      this.selectedLevelRankRoleName = this.getRoleNameFromList(this.guildConfig.roleLevelRankRoleId);
      this.selectedBoostRankRoleName = this.getRoleNameFromList(this.guildConfig.roleBoostRankRoleId);
      
    }
  }

  getRoleNameFromList(roleId: string) {
    if (roleId == null) {
      return this.guildRoleList[0].name;
    } else {
      let role = this.guildRoleList.find(r => r.id == roleId);
      return role == null ? this.guildRoleList[0].name : role.name;
    }
  }

  getChannelNameFromList(channelId: string) {
    if (channelId == null) {
      return this.guildChannelList[0].name;
    } else {
      let channel = this.guildChannelList.find(r => r.id == channelId);
      return channel == null ? this.guildChannelList[0].name : channel.name;
    }
  }

  onWelcomeChannelSelected(option: DiscordObject) {
    this.selectedWelcomeChannelName = option.name;
    this.guildConfig.welcomeChannelId = option.id;
  }

  onBirthdayChannelSelected(option: DiscordObject) {
    this.selectedBirthdayChannelName = option.name;
    this.guildConfig.birthdayChannelId = option.id;
  }

  onWelcomeRoleSelected(option: DiscordObject) {
    this.selectedWelcomeRoleName = option.name;
    this.guildConfig.welcomeRoleId = option.id;
  }

  onBirthdayRoleSelected(option: DiscordObject) {
    this.selectedBirthdayRoleName = option.name;
    this.guildConfig.birthdayRoleId = option.id;
  }

  onLevelRankRoleSelected(option: DiscordObject) {
    this.selectedLevelRankRoleName = option.name;
    this.guildConfig.roleLevelRankRoleId = option.id;
  }

  onBoostRankRoleSelected(option: DiscordObject) {
    this.selectedBoostRankRoleName = option.name;
    this.guildConfig.roleBoostRankRoleId = option.id;
  }

  addForbiddenRoleName() {
    this.forbiddenRoleName = this.forbiddenRoleName.trim();
    let found = this.roleNameBlacklist.find(r => r == this.forbiddenRoleName);
    if (found == null) {
      this.roleNameBlacklist.push(this.forbiddenRoleName);
    }
    this.forbiddenRoleName = '';
  }

  removeForbiddenRoleName(name: string) {
    this.roleNameBlacklist = this.roleNameBlacklist.filter(r => r != name);
  }

  addForbiddenRoleColor() {
    this.forbiddenRoleColor = this.forbiddenRoleColor.trim();
    let found = this.roleColorBlacklist.find(r => r == this.forbiddenRoleColor);
    if (found == null) {
      this.roleColorBlacklist.push(this.forbiddenRoleColor);
    }
    this.forbiddenRoleColor = '';
  }

  removeForbiddenRoleColor(name: string) {
    this.roleColorBlacklist = this.roleColorBlacklist.filter(r => r != name);
  }

  onSaveChanges() {
    this.guildConfig.roleNameBlacklist = this.roleNameBlacklist.join();
    this.guildConfig.roleColorBlacklist = this.roleColorBlacklist.join();


    this.updatingConfig = true;
    this.http.post<DiscordGuildConfig>(environment.urlPrefix + 'api/discord/default/config', this.guildConfig).subscribe(() => {
      this.notifierService.notify('success', 'Changes saved successfully.');
      this.updatingConfig = false;
    }, error => {
      this.updatingConfig = false;
      this.notifierService.notify('error', error.message);
    });
  }

}
