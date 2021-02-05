import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-role',
  templateUrl: './discord-role.component.html',
  styleUrls: ['./discord-role.component.css']
})
export class DiscordRoleComponent implements OnInit {

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService) { }

  ngOnInit() {
  }

}
