import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnprofesssionalComponent } from './unprofesssional.component';

describe('UnprofesssionalComponent', () => {
  let component: UnprofesssionalComponent;
  let fixture: ComponentFixture<UnprofesssionalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UnprofesssionalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UnprofesssionalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
