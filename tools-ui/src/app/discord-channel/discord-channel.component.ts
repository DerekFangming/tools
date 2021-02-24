import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';
import { DiscordChannel } from '../model/discord-channel';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-channel',
  templateUrl: './discord-channel.component.html',
  styleUrls: ['./discord-channel.component.css']
})
export class DiscordChannelComponent implements OnInit {

  loadingChannels = false;
  keyword = '';
  type = '';
  channelList: DiscordChannel[];

  currentPage = -1;
  totalPages = 0;
  totalChannels = 0;
  resultPerPage = 15;
  math = Math;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) {
    this.title.setTitle('Discord Channels');
  }

  ngOnInit() {
    this.loadChannels(0);
  }

  loadChannels(page: number) {
    if (page < 0 || (page > this.totalPages && this.totalPages != -1)) return;
    this.loadingChannels = true;
    this.currentPage = page;

    let queryParam = new HttpParams().set('limit', this.resultPerPage.toString())
      .set('offset', (this.resultPerPage * this.currentPage).toString());
    if (this.keyword.trim() != '') {
      queryParam = queryParam.set('keyword', this.keyword.trim());
    }
    if (this.type != '') {
      queryParam = queryParam.set('type', this.type);
    }
    const httpOptions = {
      params: queryParam,
      observe: 'response' as 'response'
    };
    this.http.get<DiscordChannel[]>(environment.urlPrefix + 'api/discord/default/channels', httpOptions).subscribe(res => {
      this.channelList = res.body;
      this.totalChannels = Number(res.headers.get('X-Total-Count'));
      this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1);
      this.loadingChannels = false;
    }, error => {
      this.loadingChannels = false;
      console.log(error.error);
    });
  }

  onTypeSelected(type: string) {
    this.type = type;
    this.loadChannels(0);
  }

}
