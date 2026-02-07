import '@testbed';
import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { AppComponent } from './app.component';
import { UserService } from './shared/services/user.service';
import { TokenStorageService } from './shared/services/token.storage.service';

describe('AppComponent', () => {
  let isAuthenticated$: BehaviorSubject<boolean>;
  let user$: BehaviorSubject<any>;
  let expireTimeString$: Subject<string>;

  beforeEach(async () => {
    isAuthenticated$ = new BehaviorSubject<boolean>(true);
    user$ = new BehaviorSubject<any>({ id: 1, firstName: 'Max', lastName: 'Mustermann', access: { role: 1 } });
    expireTimeString$ = new Subject<string>();

    await TestBed.configureTestingModule({
      imports: [],
      declarations: [
        AppComponent
      ],
      providers: [
        {
          provide: UserService,
          useValue: {
            isAuthenticated$,
            user$,
            checkAuthentication: () => {},
            logout: () => {},
          },
        },
        {
          provide: TokenStorageService,
          useValue: {
            expireTimeString$,
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
    const component = fixture.componentInstance;
    component.isAuthenticated = true;
    isAuthenticated$.next(true);
    user$.next({ id: 1, firstName: 'Max', lastName: 'Mustermann', access: { role: 1 } });
    expireTimeString$.next('10m');
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const brand = compiled.querySelector('.navbar-brand');
    expect(brand).not.toBeNull();
    expect(brand!.textContent).toContain('OpenFLS');
  });

  it('unsubscribe_afterDestroy_doesNotUpdateState', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    const emitted: string[] = [];

    // Given
    component.timeLeft$.subscribe(value => emitted.push(value));
    fixture.detectChanges();
    isAuthenticated$.next(true);
    user$.next({ id: 1, firstName: 'Max', lastName: 'Mustermann', access: { role: 1 } });
    expireTimeString$.next('10m');

    // When
    fixture.destroy();
    isAuthenticated$.next(false);
    user$.next({ id: 2, firstName: 'Erika', lastName: 'Muster', access: { role: 2 } });
    expireTimeString$.next('5m');

    // Then
    expect(component.isAuthenticated).toBe(true);
    expect(component.employee?.id).toBe(1);
    expect(emitted).toEqual(['10m']);
  });
});
