import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-achievement',
  templateUrl: './discord-achievement.component.html',
  styleUrls: ['./discord-achievement.component.css']
})
export class DiscordAchievementComponent implements OnInit {

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService, private modalService: NgbModal) {
    this.title.setTitle('Discord Achievements');
  }

  ngOnInit() {
  }

}
