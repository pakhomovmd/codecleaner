import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  email: string = '';
  password: string = '';
  fullName: string = '';
  error: string = '';
  success: string = '';
  showPassword: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.error = '';
    this.success = '';
    
    this.authService.register(this.email, this.password, this.fullName).subscribe({
      next: () => {
        this.success = 'Регистрация успешна! Теперь войдите.';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        console.error('Registration error:', err);
        
        // Обработка ошибок валидации
        if (err.status === 400 && err.error?.errors) {
          const errors = err.error.errors;
          const errorMessages: string[] = [];
          
          if (errors.email) errorMessages.push(errors.email);
          if (errors.password) errorMessages.push(errors.password);
          if (errors.fullName) errorMessages.push(errors.fullName);
          
          this.error = errorMessages.join('. ');
        } else if (err.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Ошибка регистрации';
        }
      }
    });
  }
}
