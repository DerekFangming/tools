import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageConverterComponent } from './image-converter.component';

describe('ImageConverterComponent', () => {
  let component: ImageConverterComponent;
  let fixture: ComponentFixture<ImageConverterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImageConverterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImageConverterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
