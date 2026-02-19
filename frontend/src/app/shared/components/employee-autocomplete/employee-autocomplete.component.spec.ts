import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeAutocompleteComponent } from './employee-autocomplete.component';

describe('EmployeeAutocompleteComponent', () => {
  let component: EmployeeAutocompleteComponent;
  let fixture: ComponentFixture<EmployeeAutocompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeAutocompleteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EmployeeAutocompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
