import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject, tap} from "rxjs";
import {environment} from "../../environments/environment";
import {Injectable} from "@angular/core";
import {Service} from "./service";

@Injectable({
  providedIn: 'root'
})
export abstract class Base<T> implements Service<T>{
  allValues$: ReplaySubject<T[]> = new ReplaySubject<T[]>();
  allValues: T[] = [];

  abstract url;

  protected constructor(
    protected http: HttpClient) { }

  abstract initialLoad();

  create(value: T) : Observable<T> {
    return this.http
      .post<T>(`${environment.api_url}${this.url}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  update(id: number, value: T) : Observable<T> {
    return this.http
      .put<T>(`${environment.api_url}${this.url}/${id}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  delete(id: number): Observable<T> {
    return this.http
      .delete<T>(`${environment.api_url}${this.url}/${id}`)
      .pipe(tap(() => this.initialLoad()));
  }

  getAll(): Observable<T[]> {
    return this.http
      .get<T[]>(`${environment.api_url}${this.url}`);
  }

  getById(id: number): Observable<T> {
    return this.http
      .get<T>(`${environment.api_url}${this.url}/${id}`);
  }
}
