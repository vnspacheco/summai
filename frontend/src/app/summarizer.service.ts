import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { SummarizeResponse } from './models';

@Injectable({
  providedIn: 'root'
})
export class SummarizerService {
  private readonly http = inject(HttpClient);

  summarize(videoUrl: string): Observable<SummarizeResponse> {
    return this.http.post<SummarizeResponse>(`${environment.apiBaseUrl}/summarize`, {
      videoUrl
    });
  }
}
