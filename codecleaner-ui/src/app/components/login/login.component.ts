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

  constructor(private authService: AuthService, private router: Router) {}

onSubmit(): void {
  console.log('Login button clicked');
  
  this.authService.login(this.email, this.password).subscribe({
    next: (response: any) => {
      console.log('Login success:', response);
      // Сохраняем пользователя
      localStorage.setItem('user', JSON.stringify(response));
      this.router.navigate(['/projects']);
    },
    error: (err) => {
      console.error('Login error:', err);
      this.error = 'Неверный email или пароль';
    }
  });
}
}