import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ServiceClientEntriesComponent } from './service-client-entries.component';

describe('ServiceClientEntriesComponent', () => {
  let component: ServiceClientEntriesComponent;
  let fixture: ComponentFixture<ServiceClientEntriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceClientEntriesComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceClientEntriesComponent);
    component = fixture.componentInstance;
  });

  it('should highlight matching entry', () => {
    component.clientSelected = true;
    component.dateDisplay = '01.01.2025';
    component.entries = [
      { id: 1, timepoint: '08:00', employeeName: 'A' },
      { id: 2, timepoint: '09:00', employeeName: 'B' }
    ];
    component.highlightId = 2;
    fixture.detectChanges();

    const highlighted = fixture.nativeElement.querySelectorAll('.beta-entry-row--highlight');
    expect(highlighted.length).toBe(1);
  });
});
