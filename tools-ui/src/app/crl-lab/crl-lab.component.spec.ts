import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CrlLabComponent } from './crl-lab.component';

describe('CrlLabComponent', () => {
  let component: CrlLabComponent;
  let fixture: ComponentFixture<CrlLabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CrlLabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CrlLabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
