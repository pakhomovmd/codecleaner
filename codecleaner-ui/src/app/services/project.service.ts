import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Project {
  id?: number;
  name: string;
  repoUrl: string;
  description?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = 'http://localhost:8080/api/projects';

  constructor(private http: HttpClient) {}

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

createProject(project: Project, userId: number): Observable<Project> {
  const request = {
    name: project.name,
    repoUrl: project.repoUrl,
    description: project.description,
    userId: userId  // <-- важно: поле называется userId
  };
  return this.http.post<Project>(this.apiUrl, request);
}

getProject(id: number): Observable<Project> {
  return this.http.get<Project>(`${this.apiUrl}/${id}`);
}
}