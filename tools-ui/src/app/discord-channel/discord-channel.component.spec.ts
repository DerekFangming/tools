import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordChannelComponent } from './discord-channel.component';

describe('DiscordChannelComponent', () => {
  let component: DiscordChannelComponent;
  let fixture: ComponentFixture<DiscordChannelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordChannelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordChannelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
