import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { environment } from 'src/environments/environment';
import { DiscordAchievement } from '../model/discord-achievement';
import { UtilsService } from '../utils.service';

@Component({
  selector: 'app-discord-achievement',
  templateUrl: './discord-achievement.component.html',
  styleUrls: ['./discord-achievement.component.css']
})
export class DiscordAchievementComponent implements OnInit {

  achievements: DiscordAchievement[];
  loadingAchievements = true;

  constructor(private http: HttpClient, private title: Title, public utils: UtilsService, private modalService: NgbModal) {
    this.title.setTitle('Discord Achievements');
  }

  ngOnInit() {
    this.loadingAchievements = true;
    this.http.get<DiscordAchievement[]>(environment.urlPrefix + 'api/discord/default/achievements').subscribe(achievements => {
      console.log(achievements);
      this.achievements = achievements;
      this.loadingAchievements = false;
    }, error => {
      this.loadingAchievements = false;
      console.log(error.error);
    });

    // this.http.post<DiscordGuildConfig>(environment.urlPrefix + 'api/discord/default/config', this.guildConfig).subscribe(() => {
    //   this.notifierService.notify('success', 'Changes saved successfully.');
    //   this.updatingConfig = false;
    // }, error => {
    //   this.updatingConfig = false;
    //   this.notifierService.notify('error', error.message);
    // });
  }

}
