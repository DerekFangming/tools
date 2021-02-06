import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';
import { DiscordRole } from '../model/discord-role';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-role',
  templateUrl: './discord-role.component.html',
  styleUrls: ['./discord-role.component.css']
})
export class DiscordRoleComponent implements OnInit {

  loadingRoles = false;
  keyword = '';
  type = '';
  roleList: DiscordRole[];

  currentPage = -1;
  totalPages = 0;
  totalRoles = 0;
  resultPerPage = 15;
  math = Math;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) {
    this.title.setTitle('Discord Roles');
  }

  ngOnInit() {
    this.loadRoles(0);
  }

  loadRoles(page: number) {
    if (page < 0 || (page > this.totalPages && this.totalPages != -1)) return;
    this.loadingRoles = true;
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
    this.http.get<DiscordRole[]>(environment.urlPrefix + 'api/discord/default/roles', httpOptions).subscribe(res => {
      this.roleList = res.body;
      this.totalRoles = Number(res.headers.get('X-Total-Count'));
      this.totalPages = Math.ceil(Number(res.headers.get('X-Total-Count')) / this.resultPerPage - 1);
      this.loadingRoles = false;
    }, error => {
      this.loadingRoles = false;
      console.log(error.error);
    });
  }

  onTypeSelected(type: string) {
    this.type = type;
    this.loadRoles(0);
  }

}
