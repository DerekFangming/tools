import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
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

  loadingUserLogs = false;
  userLogList: DiscordUserLog[];

  displayName = '';
  fromDate: any;
  toDate:any;
  action = '';

  currentPage = -1;
  totalPages = 0;
  totalLogs = 0;
  resultPerPage = 15;
  math = Math;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) {
    this.title.setTitle('Discord Insights');
  }

  ngOnInit() {
    this.loadUserLogs(0);
  }

  loadUserLogs(page: number) {
    if (page < 0 || (page > this.totalPages && this.totalPages != -1)) return;
    this.loadingUserLogs = true;
    this.currentPage = page;

    let queryParam = new HttpParams().set('limit', this.resultPerPage.toString())
      .set('offset', (this.resultPerPage * this.currentPage).toString());
    if (this.displayName.trim() != '') {
      queryParam = queryParam.set('keyword', this.displayName.trim());
    }
    if (this.action != '') {
      queryParam = queryParam.set('action', this.action);
    }
    if (this.fromDate != null) {
      queryParam = queryParam.set('from', new Date(this.fromDate.year + '-' + this.fromDate.month + '-' + this.fromDate.day).toISOString());
    }
    if (this.toDate != null) {
      queryParam = queryParam.set('to', new Date(this.toDate.year + '-' + this.toDate.month + '-' + this.toDate.day).toISOString());
    }

    const httpOptions = {
      params: queryParam,
      observe: 'response' as 'response'
    };
    this.http.get<DiscordUserLog[]>(environment.urlPrefix + 'api/discord/default/user-logs', httpOptions).subscribe(res => {
      this.userLogList = res.body;
      this.totalLogs = Number(res.headers.get('X-Total-Count'));
      this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1);
      this.loadingUserLogs = false;
    }, error => {
      this.loadingUserLogs = false;
      console.log(error.error);
    });
  }

  onActionSelected(action: string) {
    this.action = action;
    this.loadUserLogs(0);
  }

}
