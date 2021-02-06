import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from '../../environments/environment';
import { DiscordUser } from '../model/discord-user';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-user',
  templateUrl: './discord-user.component.html',
  styleUrls: ['./discord-user.component.css']
})
export class DiscordUserComponent implements OnInit {

  loadingUsers = false;
  keyword = '';
  userList: DiscordUser[];

  currentPage = -1;
  totalPages = 0;
  totalUsers = 0;
  resultPerPage = 15;
  math = Math;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) {
    this.title.setTitle('Discord Users');
  }

  ngOnInit() {
    this.loadUsers(0);
  }

  loadUsers(page: number) {
    if (page < 0 || (page > this.totalPages && this.totalPages != -1)) return;
    this.loadingUsers = true;
    this.currentPage = page;

    let queryParam = new HttpParams().set('limit', this.resultPerPage.toString())
      .set('offset', (this.resultPerPage * this.currentPage).toString());
    if (this.keyword.trim() != '') {
      queryParam = queryParam.set('keyword', this.keyword.trim());
    }

    const httpOptions = {
      params: queryParam,
      observe: 'response' as 'response'
    };
    this.http.get<DiscordUser[]>(environment.urlPrefix + 'api/discord/default/users', httpOptions).subscribe(res => {
      this.userList = res.body;
      this.totalUsers = Number(res.headers.get('X-Total-Count'));
      this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1);
      this.loadingUsers = false;
    }, error => {
      this.loadingUsers = false;
      console.log(error.error);
    });
  }

}
