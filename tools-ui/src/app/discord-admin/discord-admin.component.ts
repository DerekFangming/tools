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
  moveRoleId = '';
  moveRolePosition = 0;

  addAchMemberId = '';
  addAchAchId = 0;

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
      alert(error.message);
    });
  }

  removeRole() {
    let admin = new DiscordAdmin({memberId: this.removeMemberId, roleId: this.removeRoleId});
    this.http.post(environment.urlPrefix + 'api/discord/admin/remove-role', admin).subscribe(res => {
      alert("ok");
    }, error => {
      alert(error.message);
    });
  }

  moveRole() {
    let admin = new DiscordAdmin({memberId: this.moveRoleId, position: this.moveRolePosition});
    this.http.post(environment.urlPrefix + 'api/discord/admin/move-role', admin).subscribe(res => {
      alert("ok");
    }, error => {
      alert(error.message);
    });
  }

  startSpeed() {
    this.http.get<boolean>(environment.urlPrefix + 'api/discord/admin/speed-on').subscribe(res => {
      alert(res ? 'Started!' : 'Stopped');
    }, error => {
      alert(error.message);
    });
  }

  stopSpeed() {
    this.http.get<boolean>(environment.urlPrefix + 'api/discord/admin/speed-off').subscribe(res => {
      alert(res ? 'Started!' : 'Stopped');
    }, error => {
      alert(error.message);
    });
  }

  addAchievement() {
    let aches = [this.addAchAchId]
    this.http.post(environment.urlPrefix + 'api/discord/default/add-achievements/' + this.addAchMemberId, aches).subscribe(res => {
      alert('Done');
    }, error => {
      alert(error.message);
    });
  }

}
