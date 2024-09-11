import { Injectable } from '@angular/core';
import {Token} from "../dtos/token.model";
import {ReplaySubject} from "rxjs";
import {Router} from "@angular/router";
import {Converter} from "./converter.helper";

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
    const str = window.localStorage.getItem(USER_ID)
    return str === null ? -1 : +str;
  }

  getTokenString(): string {
    const str = window.localStorage.getItem(TOKEN_KEY)
    return str === null ? "" : str;
  }

  getExpireDateTimeString(): string {
    const str = window.localStorage.getItem(EXPIRE_DATETIME)
    return str === null ? "" : str;
  }

  getExpireTimeString(): string {
    const diffDate = new Date(this.getExpireDateTimeString()).getTime() - Date.now();
    return this.converter.convertToTimeString(diffDate);
  }

  saveToken(token: Token): void {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.setItem(TOKEN_KEY, token.token);
    window.localStorage.removeItem(USER_ID);
    window.localStorage.setItem(USER_ID, token.id === null ? "-1" : token.id.toString());
    window.localStorage.removeItem(EXPIRE_DATETIME);
    window.localStorage.setItem(EXPIRE_DATETIME, token.expiredAt.toString());
  }

  getToken(): Token | null {
    const idStr = window.localStorage.getItem(USER_ID)
    return (idStr) ?
      <Token> {
        token: window.localStorage.getItem(TOKEN_KEY),
        id: +idStr,
        expiredAt: window.localStorage.getItem(EXPIRE_DATETIME)
      } : null
  }

  destroyToken() {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_ID);
    window.localStorage.removeItem(EXPIRE_DATETIME);
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
