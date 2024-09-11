import {Observable} from "rxjs";

export interface Service<T> {
  create(value: T) : Observable<T>;

  update(id: number, value: T) : Observable<T>;

  delete(id: number): Observable<T>;

  getAll(): Observable<T[]>;

  getById(id: number): Observable<T>;
}
