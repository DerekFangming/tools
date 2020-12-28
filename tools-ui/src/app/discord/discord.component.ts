import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';
import { DiscordUserLog } from '../model/discord-user-log';

@Component({
  selector: 'app-discord',
  templateUrl: './discord.component.html',
  styleUrls: ['./discord.component.css']
})
export class DiscordComponent implements OnInit {

  tab = 'logs';
  loading = true;

  userLogList: DiscordUserLog[];

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle('Discord Insights');
  }

  ngOnInit() {
    this.http.get<DiscordUserLog[]>(environment.urlPrefix + 'api/discord/default/user-logs').subscribe(userLogList => {
      this.userLogList = userLogList;
      this.loading = false;
    }, error => {
      this.loading = false;
      console.log(error.error);
    });
  }

  onTabSelected(newTab: string) {
    this.tab = newTab;
  }

  getCreatedTime(time: string) {
    return new Date(time).toLocaleString();
  }

  getType(input: string) {
    if (input == null) return '';

    return input
    .split("_")
    .reduce((res, word, i) =>
      `${res}${word.charAt(0).toUpperCase()}${word
        .substr(1)
        .toLowerCase()}`,
      ""
    );
  }

}
