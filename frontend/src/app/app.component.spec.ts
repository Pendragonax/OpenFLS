import '@testbed';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { UserService } from './shared/services/user.service';
import { TokenStorageService } from './shared/services/token.storage.service';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        {
          provide: UserService,
          useValue: {
            isAuthenticated$: of(true),
            user$: of({ firstName: 'Max', lastName: 'Mustermann', access: { role: 1 } }),
            checkAuthentication: () => {},
            logout: () => {},
          },
        },
        {
          provide: TokenStorageService,
          useValue: {
            expireTimeString$: of(''),
          },
        },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'OpenFLS'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('OpenFLS');
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const brand = compiled.querySelector('.navbar-brand');
    expect(brand).not.toBeNull();
    expect(brand!.textContent).toContain('OpenFLS');
  });
});
