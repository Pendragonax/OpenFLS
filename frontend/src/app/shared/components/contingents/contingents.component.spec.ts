import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContingentsComponent } from './contingents.component';

describe('ContingentsComponent', () => {
  let component: ContingentsComponent;
  let fixture: ComponentFixture<ContingentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContingentsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContingentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
