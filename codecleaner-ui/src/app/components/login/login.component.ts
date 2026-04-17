import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  error: string = '';
  showPassword: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    console.log('Login attempt with:', this.email);
    this.error = '';
    
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (response) => {
        console.log('Login successful, tokens received:', response);
        // Сохраняем токены
        this.authService.saveTokens(response.accessToken, response.refreshToken);
        
        // Получаем данные пользователя
        this.authService.getCurrentUser().subscribe({
          next: (user) => {
            console.log('User data received:', user);
            this.authService.saveUser(user);
            console.log('Navigating to /projects');
            this.router.navigate(['/projects']).then(success => {
              console.log('Navigation result:', success);
              if (!success) {
                console.error('Navigation failed!');
              }
            });
          },
          error: (err) => {
            console.error('Error fetching user:', err);
            this.error = 'Ошибка получения данных пользователя';
          }
        });
      },
      error: (err) => {
        console.error('Login error:', err);
        this.error = 'Неверный email или пароль';
      }
    });
  }
}
