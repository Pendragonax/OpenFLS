import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionSelectComponent } from './institution-select.component';

describe('InstitutionSelectComponent', () => {
  let component: InstitutionSelectComponent;
  let fixture: ComponentFixture<InstitutionSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionSelectComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InstitutionSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
