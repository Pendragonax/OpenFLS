import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {
  BehaviorSubject,
  catchError,
  Observable,
  ReplaySubject, Subject,
  tap,
  throwError
} from "rxjs";
import {TokenStorageService} from "./token.storage.service";
import {environment} from "../../environments/environment";
import {Token} from "../models/token.model";
import {EmployeeDto} from "../dtos/employee-dto.model";
import {Router} from "@angular/router";
import {PasswordDto} from "../dtos/password-dto.model";

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class UserService {
  user$ = new ReplaySubject<EmployeeDto>();
  isAuthenticated$ = new ReplaySubject<boolean>();
  roles$ = new BehaviorSubject<string[]>([]);
  leadingInstitutions$ = new BehaviorSubject<number[]>([]);
  affiliatedInstitutions$ = new BehaviorSubject<number[]>([]);
  readableInstitutions$ = new BehaviorSubject<number[]>([]);
  writeableInstitutions$ = new BehaviorSubject<number[]>([]);
  isAdmin$ = new BehaviorSubject(false);

  constructor(
    private http: HttpClient,
    private tokenService: TokenStorageService,
    private router: Router
  ) {
    this.loadLocalToken();
  }

  login(username: string, password: string): Observable<Token> {
    return this.http
      .post<Token>(
        `${environment.api_url}login`,
        { username, password },
        httpOptions)
      .pipe(
        tap(data => {
          this.saveToken(data);
          this.router.navigate(["/"]).then();
        }),
        catchError(this.handleError)
      );
  }

  logout() {
    this.destroyToken();
    this.router.navigate(["/login"]).then(() => window.location.reload());
  }

  changePassword(passwordDto: PasswordDto): Observable<any> {
    return this.http.post(`${environment.api_url}password`, passwordDto);
  }

  checkAuthentication() {
    const token = this.tokenService.getToken();

    if (token) {
      this.tryGetRequest();
    } else {
      this.destroyToken();
    }
  }

  private loadLocalToken() {
    const token = this.tokenService.getToken();

    if (token) {
      this.isAuthenticated$.next(true);
      this.loadUser();
    } else {
      this.destroyToken();
    }
  }

  private saveToken(token: Token) {
    this.tokenService.saveToken(token);
    this.isAuthenticated$.next(true);
    this.loadUser();
  }

  loadUser() {
    this.getUser().subscribe({
      next: data => {
        this.roles$.next(UserService.getRoles(data));
        this.leadingInstitutions$.next(UserService.getLeadingInstitutions(data));
        this.affiliatedInstitutions$.next(UserService.getAffiliatedInstitutions(data));
        this.readableInstitutions$.next(UserService.getReadableInstitutions(data));
        this.writeableInstitutions$.next(UserService.getWriteableInstitutions(data));
        this.isAdmin$.next((data.access?.role ?? 99) === 1);
        this.user$.next(data);
      },
      error: err => this.handleError(err)
    });
  }

  private destroyToken() {
    this.tokenService.destroyToken();
    this.isAuthenticated$.next(false);
  }

  private getUser(): Observable<EmployeeDto> {
    return this.http.get<EmployeeDto>(`${environment.api_url}user`);
  }

  private tryGetRequest(): Observable<any> {
    return this.http.get<EmployeeDto>(`${environment.api_url}`);
  }

  private handleError(error: HttpErrorResponse) {
    this.destroyToken();
    this.router.navigate(["/login"]).then();

    return throwError(() => new Error(`Something bad happened; please try again later. ${error.message}`));
  }

  private static getRoles(employee: EmployeeDto): string[] {
    const roles = ['user']

    if ((employee.access?.role ?? 99) <= 1)  {
      roles.push('admin', 'leader');
    } else if ((employee.access?.role ?? 99) <= 2) {
      roles.push('leader');
    }

    return roles
  }

  private static getLeadingInstitutions(employee: EmployeeDto): number[] {
    if (employee === undefined)
      return [];

    return employee?.permissions
      .filter(value => value.changeInstitution)
      .map(value => value.institutionId);
  }

  private static getAffiliatedInstitutions(employee: EmployeeDto): number[] {
    if (employee == null)
      return [];

    return employee?.permissions
      .filter(value => value.affiliated)
      .map(value => value.institutionId);
  }

  private static getWriteableInstitutions(employee: EmployeeDto): number[] {
    if (employee === undefined)
      return [];

    return employee?.permissions
      .filter(value => value.writeEntries)
      .map(value => value.institutionId);
  }

  private static getReadableInstitutions(employee: EmployeeDto): number[] {
      if (employee === undefined)
        return [];

      return employee?.permissions
        .filter(value => value.readEntries)
        .map(value => value.institutionId);
  }
}
