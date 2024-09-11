import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllServicesComponent } from './all-services.component';

describe('AllServicesComponent', () => {
  let component: AllServicesComponent;
  let fixture: ComponentFixture<AllServicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllServicesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AllServicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
