import { Component, OnInit } from '@angular/core';
import { DiscordUser } from '../model/discord-user';

@Component({
  selector: 'app-discord-user',
  templateUrl: './discord-user.component.html',
  styleUrls: ['./discord-user.component.css']
})
export class DiscordUserComponent implements OnInit {

  loadingUser = false;
  userLogList: DiscordUser[];

  constructor() { }

  ngOnInit() {
    console.log('111111111111111111111');
  }

}
