import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionNewComponent } from './institution-new.component';

describe('InstitutionNewComponent', () => {
  let component: InstitutionNewComponent;
  let fixture: ComponentFixture<InstitutionNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InstitutionNewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InstitutionNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
