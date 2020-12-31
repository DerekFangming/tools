import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap';
import { NotifierService } from 'angular-notifier';
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
  updatingConfig = false;

  userLogList: DiscordUserLog[];
  pagedUserLogList: DiscordUserLog[];
  guildConfig: DiscordGuildConfig;
  guildRoleList: DiscordObject[];
  guildChannelList: DiscordObject[];

  displayName = '';
  fromDate: any;
  toDate:any;
  action = '';

  selectedWelcomeChannelName = '';
  selectedBirthdayChannelName = '';
  
  selectedWelcomeRoleName = '';
  selectedBirthdayRoleName = '';

  currentPage = 0;
  totalPages = 0;
  resultPerPage = 15;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService, private activatedRoute: ActivatedRoute,
      private router: Router, private notifierService: NotifierService) {
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
    this.currentPage = 0;
    this.http.get<DiscordUserLog[]>(environment.urlPrefix + 'api/discord/default/user-logs').subscribe(userLogList => {
      this.userLogList = userLogList.filter(ul => {
        let created = new Date(ul.created).toLocaleDateString('en', {year: 'numeric', month: '2-digit', day: 'numeric'}).split('/');

        let nameMatched = this.displayName.trim() == '' || ul.name.toLowerCase().includes(this.displayName.trim().toLowerCase());
        let fromMatched = this.fromDate == null || !new NgbDate(Number(created[2]), Number(created[0]), Number(created[1]))
          .before({ year: this.fromDate.year, month: this.fromDate.month, day: this.fromDate.day });
        let toMatched = this.toDate == null || !new NgbDate(Number(created[2]), Number(created[0]), Number(created[1]))
          .after({ year: this.toDate.year, month: this.toDate.month, day: this.toDate.day });
        let actionMatched = this.action == '' || this.action == ul.action.toLowerCase();

        return nameMatched && fromMatched && toMatched && actionMatched;
      });
      this.totalPages = Math.ceil(this.userLogList.length / this.resultPerPage);
      this.onPageIndexSelected(1);
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
    this.guildConfig = null;
    this.guildChannelList = null;
    this.guildRoleList = null;

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
        this.selectedWelcomeChannelName = this.guildChannelList[0].name;
      } else {
        this.selectedWelcomeChannelName = this.guildChannelList.find(c => c.id == this.guildConfig.welcomeChannelId).name;
      }

      if (this.guildConfig.birthdayChannelId == null) {
        this.selectedBirthdayChannelName = this.guildChannelList[0].name;
      } else {
        this.selectedBirthdayChannelName = this.guildChannelList.find(c => c.id == this.guildConfig.birthdayChannelId).name;
      }

      if (this.guildConfig.welcomeRoleId == null) {
        this.selectedWelcomeRoleName = this.guildRoleList[0].name;
      } else {
        this.selectedWelcomeRoleName = this.guildRoleList.find(r => r.id == this.guildConfig.welcomeRoleId).name;
      }

      if (this.guildConfig.birthdayRoleId == null) {
        this.selectedBirthdayRoleName = this.guildRoleList[0].name;
      } else {
        this.selectedBirthdayRoleName = this.guildRoleList.find(r => r.id == this.guildConfig.birthdayRoleId).name;
      }
    }
  }

  onActionSelected(action: string) {
    this.action = action;
  }

  onPageIndexSelected(newPage: number) {
    if(newPage != this.currentPage) {
      this.currentPage = newPage;

      let startIndex = newPage - 1;
      if (newPage == this.totalPages) {
        this.pagedUserLogList = this.userLogList.slice(startIndex * this.resultPerPage, this.userLogList.length);
      } else {
        this.pagedUserLogList = this.userLogList.slice(startIndex * this.resultPerPage, newPage * this.resultPerPage);
      }
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

  onSaveCahnges() {
    console.log(this.guildConfig);
    this.updatingConfig = true;
    this.http.post<DiscordGuildConfig>(environment.urlPrefix + 'api/discord/default/config', this.guildConfig).subscribe(() => {
      this.notifierService.notify('success', 'Changes saved successfully.');
      this.updatingConfig = false;
    }, error => {
      this.updatingConfig = false;
      this.notifierService.notify('error', error);
    });
  }

}
