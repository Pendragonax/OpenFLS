import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { ClientAutocompleteComponent } from './client-autocomplete.component';

describe('ClientAutocompleteComponent', () => {
  let component: ClientAutocompleteComponent;
  let fixture: ComponentFixture<ClientAutocompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClientAutocompleteComponent],
      imports: [
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatFormFieldModule,
        MatInputModule,
      ],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ClientAutocompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
