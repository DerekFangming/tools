import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordAdminComponent } from './discord-admin.component';

describe('DiscordAdminComponent', () => {
  let component: DiscordAdminComponent;
  let fixture: ComponentFixture<DiscordAdminComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscordAdminComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscordAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
