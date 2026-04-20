import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AnalysisMethod {
  name: string;
  displayName: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private apiUrl = `${environment.apiUrl}/analysis`;

  constructor(private http: HttpClient) {}

  getAnalysisMethods(): Observable<AnalysisMethod[]> {
    return this.http.get<AnalysisMethod[]>(`${this.apiUrl}/methods`);
  }

  getAnalysesByProject(projectId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/project/${projectId}`);
  }

  uploadAndAnalyze(projectId: number, file: File, method: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('method', method);
    return this.http.post(`${this.apiUrl}/upload/${projectId}`, formData);
  }

  getAnalysisResult(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}`);
  }

  getAnalysisDetails(sessionId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/session/${sessionId}`);
  }

  deleteAnalysis(sessionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/session/${sessionId}`);
  }

  deleteAllAnalysesByProject(projectId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/project/${projectId}/all`);
  }
  
  analyzeClonedRepository(projectId: number, method: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/analyze-cloned/${projectId}?method=${method}`, {});
  }
}
