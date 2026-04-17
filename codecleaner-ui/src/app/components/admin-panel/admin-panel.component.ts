import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AdminService, User, AdminStats } from '../../services/admin.service';
import { ProjectService } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.css']
})
export class AdminPanelComponent implements OnInit {
  users: User[] = [];
  stats: AdminStats | null = null;
  loading = true;
  selectedUser: User | null = null;
  userProjects: any[] = [];
  loadingProjects = false;

  constructor(
    private adminService: AdminService,
    private projectService: ProjectService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Проверяем, что пользователь - админ
    const user = this.authService.getUser();
    if (!user || user.role !== 'ADMIN') {
      alert('Доступ запрещён. Только для администраторов.');
      this.router.navigate(['/projects']);
      return;
    }

    this.loadUsers();
    this.loadStats();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.loading = false;
        if (err.status === 403) {
          alert('Доступ запрещён');
          this.router.navigate(['/projects']);
        }
      }
    });
  }

  loadStats(): void {
    this.adminService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (err) => console.error('Error loading stats:', err)
    });
  }

  viewUserProjects(user: User): void {
    this.selectedUser = user;
    this.loadingProjects = true;
    this.adminService.getUserProjects(user.id).subscribe({
      next: (data) => {
        this.userProjects = data;
        this.loadingProjects = false;
      },
      error: (err) => {
        console.error('Error loading user projects:', err);
        this.loadingProjects = false;
      }
    });
  }

  closeUserProjects(): void {
    this.selectedUser = null;
    this.userProjects = [];
  }

  deleteUser(userId: number, userEmail: string): void {
    if (confirm(`Вы уверены, что хотите удалить пользователя "${userEmail}"? Все его проекты и анализы будут удалены!`)) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          console.log('User deleted successfully');
          this.loadUsers();
          this.loadStats();
          if (this.selectedUser?.id === userId) {
            this.closeUserProjects();
          }
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('Ошибка при удалении пользователя: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  deleteProject(projectId: number, projectName: string): void {
    if (confirm(`Вы уверены, что хотите удалить проект "${projectName}"?`)) {
      this.projectService.deleteProject(projectId).subscribe({
        next: () => {
          console.log('Project deleted successfully');
          if (this.selectedUser) {
            this.viewUserProjects(this.selectedUser);
          }
          this.loadStats();
        },
        error: (err) => {
          console.error('Error deleting project:', err);
          alert('Ошибка при удалении проекта: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
