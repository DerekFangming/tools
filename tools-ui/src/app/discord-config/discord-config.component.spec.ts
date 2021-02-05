import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordConfigComponent } from './discord-config.component';

describe('DiscordConfigComponent', () => {
  let component: DiscordConfigComponent;
  let fixture: ComponentFixture<DiscordConfigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordConfigComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
