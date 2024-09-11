import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyServicesComponent } from './my-services.component';

describe('ServicesComponent', () => {
  let component: MyServicesComponent;
  let fixture: ComponentFixture<MyServicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyServicesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyServicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
