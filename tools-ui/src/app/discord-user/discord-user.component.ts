import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { DiscordUser } from '../model/discord-user';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-user',
  templateUrl: './discord-user.component.html',
  styleUrls: ['./discord-user.component.css']
})
export class DiscordUserComponent implements OnInit {

  loadingUser = false;
  userLogList: DiscordUser[];

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) { }

  ngOnInit() {
    console.log('111111111111111111111');
  }

}
