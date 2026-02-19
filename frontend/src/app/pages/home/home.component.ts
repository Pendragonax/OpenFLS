import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../shared/services/user.service";
import {combineLatest, map, Observable, ReplaySubject, shareReplay, tap} from "rxjs";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {EmployeeDto} from "../../shared/dtos/employee-dto.model";
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from "@angular/forms";
import {PasswordDto} from "../../shared/dtos/password-dto.model";
import {InstitutionService} from "../../shared/services/institution.service";
import {DtoCombinerService} from "../../shared/services/dto-combiner.service";
import {HelperService} from "../../shared/services/helper.service";
import {createMatchingPasswordsValidator} from "../../shared/validators/matching-passwords.validator";
import {PermissionRow} from "./models/permission-row.model";

interface HomeViewModel {
  employee: EmployeeDto;
  username: string;
  role: string;
  permissions: PermissionRow[];
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: false
})
export class HomeComponent implements OnInit {

  private readonly destroyRef = inject(DestroyRef);
  private readonly pwdPattern = '(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&].{8,}';
  readonly favorite$ = new ReplaySubject<boolean>(1);
  readonly currentEmployee$ = new ReplaySubject<EmployeeDto>(1);
  private readonly tabKeys = ['favorites', 'hours', 'general'] as const;
  selectedTabIndex = 0;

  readonly homeVm$: Observable<HomeViewModel>;

  readonly passwordDto = new PasswordDto();
  isSubmitting = false;

  // FORMs
  readonly passwordForm: FormGroup<{
    oldPassword: FormControl<string>;
    password1: FormControl<string>;
    password2: FormControl<string>;
  }>;

  get oldPasswordControl() { return this.passwordForm.controls.oldPassword; }

  get password1Control() { return this.passwordForm.controls.password1; }

  constructor(
    private formBuilder: NonNullableFormBuilder,
    private helperService: HelperService,
    private userService: UserService,
    private institutionService: InstitutionService,
    private dtoCombinerService: DtoCombinerService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.homeVm$ = combineLatest([
      this.userService.user$,
      this.institutionService.allValues$
    ]).pipe(
      map(([employee, institutions]): HomeViewModel => ({
        employee,
        username: employee.access?.username ?? "",
        role: HomeComponent.getRole(employee.access?.role),
        permissions: institutions != null ?
          this.dtoCombinerService.combinePermissionsByEmployee(employee, institutions) : []
      })),
      tap((viewModel) => this.currentEmployee$.next(viewModel.employee)),
      shareReplay({ bufferSize: 1, refCount: true })
    );
    this.passwordForm = this.formBuilder.group({
      oldPassword: this.formBuilder.control(''),
      password1: this.formBuilder.control('', {
        validators: [Validators.required, Validators.pattern(this.pwdPattern), Validators.minLength(8)]
      }),
      password2: this.formBuilder.control('')
    }, { validators: createMatchingPasswordsValidator });
  }

  ngOnInit(): void {
    this.initFormSubscriptions();
    this.favorite$.next(true);
    this.initTabSync();
  }

  refreshUser() {
    this.userService.loadUser();
  }

  private initFormSubscriptions() {
    this.oldPasswordControl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => this.passwordDto.oldPassword = value);
    this.password1Control.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => this.passwordDto.newPassword = value);
  }

  private initTabSync() {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const tabKey = params.get('tab');
        const index = tabKey ? this.tabKeys.indexOf(tabKey as (typeof this.tabKeys)[number]) : -1;
        const nextIndex = index >= 0 ? index : 0;
        if (nextIndex !== this.selectedTabIndex) {
          this.selectedTabIndex = nextIndex;
        }
      });
  }

  onTabIndexChange(index: number) {
    const tabKey = this.tabKeys[index] ?? this.tabKeys[0];
    const currentTab = this.route.snapshot.queryParamMap.get('tab');
    if (currentTab === tabKey) {
      return;
    }
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab: tabKey },
      queryParamsHandling: 'merge'
    });
  }

  resetPasswordForm() {
    this.passwordForm.reset({
      oldPassword: '',
      password1: '',
      password2: ''
    });
  }

  updatePassword() {
    if (this.isSubmitting || this.passwordForm.invalid)
      return;

    this.isSubmitting = true;

    this.userService.changePassword(this.passwordDto).subscribe({
      next: () => {
        this.helperService.openSnackBar("Passwort erfolgreich geändert!");
        this.resetPasswordForm();
        this.isSubmitting = false;
      },
      error: () => {
        this.helperService.openSnackBar("Passwort konnte nicht geändert werden!");
        this.isSubmitting = false;
      }
    })
  }

  private static getRole(role: number | undefined): string {
    if (role === undefined)
      return "Mitarbeiter";

    switch(role) {
      case 1:
        return "Administrator";
      case 2:
        return "Leitungskraft";
      default:
        return "Mitarbeiter"
    }
  }
}
