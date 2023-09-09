import { Injectable } from '@angular/core';
import {Token} from "../models/token.model";
import {Converter} from "../shared/converter.helper";
import {ReplaySubject} from "rxjs";
import {Router} from "@angular/router";

const TOKEN_KEY = 'auth-token';
const USER_ID = 'auth-user-id';
const EXPIRE_DATETIME = 'auth-token-expire';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  expireTimeString$: ReplaySubject<string> = new ReplaySubject<string>();
  tokenValid$: ReplaySubject<boolean> = new ReplaySubject();

  private tokenExpireTimerId;

  constructor(
    private converter: Converter,
    private router: Router
  ) {
    this.startExpireTimeInterval();
  }

  getId(): number {
    const str = window.sessionStorage.getItem(USER_ID)
    return str === null ? -1 : +str;
  }

  getTokenString(): string {
    const str = window.sessionStorage.getItem(TOKEN_KEY)
    return str === null ? "" : str;
  }

  getExpireDateTimeString(): string {
    const str = window.sessionStorage.getItem(EXPIRE_DATETIME)
    return str === null ? "" : str;
  }

  getExpireTimeString(): string {
    const diffDate = new Date(this.getExpireDateTimeString()).getTime() - Date.now();
    return this.converter.convertToTimeString(diffDate);
  }

  saveToken(token: Token): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token.token);
    window.sessionStorage.removeItem(USER_ID);
    window.sessionStorage.setItem(USER_ID, token.id === null ? "-1" : token.id.toString());
    window.sessionStorage.removeItem(EXPIRE_DATETIME);
    window.sessionStorage.setItem(EXPIRE_DATETIME, token.expiredAt.toString());
  }

  getToken(): Token | null {
    const idStr = window.sessionStorage.getItem(USER_ID)
    return (idStr) ?
      <Token> {
        token: window.sessionStorage.getItem(TOKEN_KEY),
        id: +idStr,
        expiredAt: window.sessionStorage.getItem(EXPIRE_DATETIME)
      } : null
  }

  destroyToken() {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.removeItem(USER_ID);
    window.sessionStorage.removeItem(EXPIRE_DATETIME);
  }

  checkExpireTime() {
    this.expireTimeString$.next(this.getExpireTimeString().toString())
    const diffDate = new Date(this.getExpireDateTimeString()).getTime() - Date.now()

    if (diffDate < 0) {
      clearInterval(this.tokenExpireTimerId);
      this.destroyToken();
      this.router.navigate(["/login"]).then(() => window.location.reload());
      this.tokenValid$.next(false);
    } else {
      this.tokenValid$.next(true);
    }
  }

  private startExpireTimeInterval() {
    this.tokenExpireTimerId = setInterval(() => this.checkExpireTime(), 500);
  }
}
