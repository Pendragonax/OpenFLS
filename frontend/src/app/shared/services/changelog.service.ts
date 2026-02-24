import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChangelogService {

  constructor(private http: HttpClient) {}

  getLatestAsMarkdown(): Observable<string> {
    return this.http
      .get(`${environment.api_url}changelog/latest.md`, { responseType: 'text' })
      .pipe(map(markdown => this.normalizeApiUrls(markdown)));
  }

  private normalizeApiUrls(markdown: string): string {
    const apiBase = environment.api_url.endsWith('/')
      ? environment.api_url
      : `${environment.api_url}/`;

    return markdown.replace(/\]\(\/api\//g, `](${apiBase}`);
  }
}
