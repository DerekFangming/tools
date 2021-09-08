import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordLogComponent } from './discord-log.component';

describe('DiscordLogComponent', () => {
  let component: DiscordLogComponent;
  let fixture: ComponentFixture<DiscordLogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordLogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
