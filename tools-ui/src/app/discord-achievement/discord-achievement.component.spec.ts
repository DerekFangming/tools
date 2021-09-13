import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordAchievementComponent } from './discord-achievement.component';

describe('DiscordAchievementComponent', () => {
  let component: DiscordAchievementComponent;
  let fixture: ComponentFixture<DiscordAchievementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordAchievementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordAchievementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
