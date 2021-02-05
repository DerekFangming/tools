import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordRoleComponent } from './discord-role.component';

describe('DiscordRoleComponent', () => {
  let component: DiscordRoleComponent;
  let fixture: ComponentFixture<DiscordRoleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordRoleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordRoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
