import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProjectService, Project } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';

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
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userStr = sessionStorage.getItem('user');
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
    if (!this.newProject.name) {
      alert('Заполните название проекта');
      return;
    }
    
    // Валидация длины имени
    if (this.newProject.name.length < 3) {
      alert('Название проекта должно содержать минимум 3 символа');
      return;
    }
    
    // Валидация URL - добавляем https:// если не указан протокол (только если URL указан)
    if (this.newProject.repoUrl && this.newProject.repoUrl.trim()) {
      let repoUrl = this.newProject.repoUrl.trim();
      
      // Проверяем, что это GitHub URL
      if (!repoUrl.includes('github.com')) {
        alert('Допускаются только GitHub репозитории (https://github.com/user/repo)');
        return;
      }
      
      if (!repoUrl.startsWith('http://') && !repoUrl.startsWith('https://')) {
        repoUrl = 'https://' + repoUrl;
      }
      
      this.newProject.repoUrl = repoUrl;
    } else {
      // Если URL не указан, устанавливаем пустую строку
      this.newProject.repoUrl = '';
    }
    
    if (!this.currentUserId) {
      alert('Пользователь не найден. Пожалуйста, войдите заново.');
      this.router.navigate(['/login']);
      return;
    }
    
    console.log('Creating project with data:', {
      name: this.newProject.name,
      repoUrl: this.newProject.repoUrl,
      description: this.newProject.description,
      userId: this.currentUserId
    });
    
    this.projectService.createProject(this.newProject, this.currentUserId).subscribe({
      next: () => {
        console.log('Project created successfully');
        alert('Проект успешно создан!');
        this.loadProjects();
        this.newProject = { name: '', repoUrl: '', description: '' };
      },
      error: (err) => {
        console.error('Error creating project:', err);
        console.error('Error details:', err.error);
        
        let errorMessage = 'Ошибка при создании проекта';
        if (err.error?.errors) {
          // Обработка ошибок валидации
          const validationErrors = Object.entries(err.error.errors)
            .map(([field, message]) => `${field}: ${message}`)
            .join('\n');
          errorMessage += ':\n' + validationErrors;
        } else if (err.error?.message) {
          errorMessage += ': ' + err.error.message;
        } else if (err.message) {
          errorMessage += ': ' + err.message;
        }
        
        alert(errorMessage);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openProject(projectId: number): void {
    this.router.navigate(['/project', projectId]);
  }

  deleteProject(event: Event, projectId: number, projectName: string): void {
    event.stopPropagation(); // Предотвращаем открытие проекта при клике на удаление
    
    if (confirm(`Вы уверены, что хотите удалить проект "${projectName}"? Это действие нельзя отменить.`)) {
      this.projectService.deleteProject(projectId).subscribe({
        next: () => {
          console.log('Project deleted successfully');
          this.loadProjects();
        },
        error: (err) => {
          console.error('Error deleting project:', err);
          alert('Ошибка при удалении проекта: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  isAdmin(): boolean {
    const user = this.authService.getUser();
    return user?.role === 'ADMIN';
  }
}