import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProjectService, Project } from '../../services/project.service';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  newProject: Project = { name: '', repoUrl: '', description: '' };
  loading: boolean = false;
  currentUserId: number | null = null;

  constructor(
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      this.router.navigate(['/login']);
      return;
    }
    
    try {
      const user = JSON.parse(userStr);
      this.currentUserId = user.id;
      console.log('Current user ID:', this.currentUserId);
      this.loadProjects();
    } catch (e) {
      console.error('Error parsing user:', e);
      this.router.navigate(['/login']);
    }
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading projects:', err);
        this.loading = false;
        if (err.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  createProject(): void {
    if (!this.newProject.name || !this.newProject.repoUrl) {
      alert('Заполните название и URL репозитория');
      return;
    }
    
    if (!this.currentUserId) {
      alert('Пользователь не найден. Пожалуйста, войдите заново.');
      this.router.navigate(['/login']);
      return;
    }
    
    console.log('Creating project with userId:', this.currentUserId);
    
    this.projectService.createProject(this.newProject, this.currentUserId).subscribe({
      next: () => {
        console.log('Project created successfully');
        this.loadProjects();
        this.newProject = { name: '', repoUrl: '', description: '' };
      },
      error: (err) => {
        console.error('Error creating project:', err);
        alert('Ошибка при создании проекта: ' + (err.error?.message || err.message));
      }
    });
  }

  logout(): void {
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  openProject(projectId: number): void {
  this.router.navigate(['/project', projectId]);
}
}