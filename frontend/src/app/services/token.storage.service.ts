import { Injectable } from '@angular/core';
import {Token} from "../models/token.model";
import {Converter} from "../shared/converter.helper";
import {BehaviorSubject, ReplaySubject} from "rxjs";

const TOKEN_KEY = 'auth-token';
const USER_ID = 'auth-user-id';
const EXPIRE_DATETIME = 'auth-token-expire';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  expireTimeString$: ReplaySubject<string> = new ReplaySubject<string>();

  constructor(
    private converter: Converter
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
    return this.converter.convertToTimeString(new Date(this.getExpireDateTimeString()).getTime() - Date.now());
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

  private startExpireTimeInterval() {
    setInterval(() => this.expireTimeString$.next(this.getExpireTimeString().toString()), 500);
  }
}
