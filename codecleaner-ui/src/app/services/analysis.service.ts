import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private apiUrl = 'http://localhost:8080/api/analysis';

  constructor(private http: HttpClient) {}

  getAnalysesByProject(projectId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/project/${projectId}`);
  }

  uploadAndAnalyze(projectId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/upload/${projectId}`, formData);
  }

  getAnalysisResult(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}`);
  }

  getAnalysisDetails(sessionId: number): Observable<any> {
  return this.http.get(`${this.apiUrl}/session/${sessionId}`);
}
}