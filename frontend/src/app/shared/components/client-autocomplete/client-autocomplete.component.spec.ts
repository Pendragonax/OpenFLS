import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientAutocompleteComponent } from './client-autocomplete.component';

describe('ClientAutocompleteComponent', () => {
  let component: ClientAutocompleteComponent;
  let fixture: ComponentFixture<ClientAutocompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientAutocompleteComponent]
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
