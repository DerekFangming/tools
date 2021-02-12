import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';
import { DiscordAdmin } from '../model/discord-admin';

@Component({
  selector: 'app-discord-admin',
  templateUrl: './discord-admin.component.html',
  styleUrls: ['./discord-admin.component.css']
})
export class DiscordAdminComponent implements OnInit {

  addMemberId = '';
  addRoleId = '';
  removeMemberId = '';
  removeRoleId = '';

  constructor(private http: HttpClient, private title: Title) {
    this.title.setTitle('Discord Roles');
  }

  ngOnInit() {
  }

  addRole() {
    let admin = new DiscordAdmin({memberId: this.addMemberId, roleId: this.addRoleId});
    this.http.post(environment.urlPrefix + 'api/discord/admin/add-role', admin).subscribe(res => {
      alert("ok");
    }, error => {
      alert(error.error);
      console.log(error.error);
    });
  }

  removeRole() {
    let admin = new DiscordAdmin({memberId: this.removeMemberId, roleId: this.removeRoleId});
    this.http.post(environment.urlPrefix + 'api/discord/admin/remove-role', admin).subscribe(res => {
      alert("ok");
    }, error => {
      alert(error.error);
      console.log(error.error);
    });
  }

}
