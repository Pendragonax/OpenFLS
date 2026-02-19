import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {InstitutionDto} from "../dtos/institution-dto.model";
import {Observable, ReplaySubject, tap} from "rxjs";
import {ReadableInstitutionDto} from "../dtos/institution-readable-dto.model";
import {environment} from "../../../environments/environment";
import {CreateInstitutionDto} from "../dtos/institution-create-dto";
import {UpdateInstitutionDto} from "../dtos/institution-update-dto";

// TODO: Refactor service
@Injectable({
  providedIn: 'root'
})
export class InstitutionService {
  allValues$: ReplaySubject<InstitutionDto[]> = new ReplaySubject<InstitutionDto[]>();
  url = "institutions";

  constructor(
    protected http: HttpClient
  ) {
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  create(value: CreateInstitutionDto) : Observable<CreateInstitutionDto> {
    return this.http
      .post<CreateInstitutionDto>(`${environment.api_url}${this.url}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  update(id: number, value: UpdateInstitutionDto) : Observable<UpdateInstitutionDto> {
    return this.http
      .put<UpdateInstitutionDto>(`${environment.api_url}${this.url}/${id}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  delete(id: number): Observable<InstitutionDto> {
    return this.http
      .delete<InstitutionDto>(`${environment.api_url}${this.url}/${id}`)
      .pipe(tap(() => this.initialLoad()));
  }

  getAll(): Observable<InstitutionDto[]> {
    return this.http
      .get<InstitutionDto[]>(`${environment.api_url}${this.url}`);
  }

  getById(id: number): Observable<InstitutionDto> {
    return this.http
      .get<InstitutionDto>(`${environment.api_url}${this.url}/${id}`);
  }

  getAllReadable(): Observable<ReadableInstitutionDto[]> {
    return this.http
      .get<ReadableInstitutionDto[]>(`${environment.api_url}${this.url}/readable`);
  }
}
