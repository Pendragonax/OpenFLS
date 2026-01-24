import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformationRowComponent } from './information-row.component';

describe('InformationRowComponent', () => {
  let component: InformationRowComponent;
  let fixture: ComponentFixture<InformationRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InformationRowComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InformationRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
