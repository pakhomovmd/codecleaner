import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: any = {
    email: '',
    fullName: '',
    role: ''
  };
  loading = true;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.loading = true;
    
    // Получаем email из localStorage
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const savedUser = JSON.parse(userStr);
      this.user = savedUser;
      this.loading = false;
    } else {
      // Если нет в localStorage, пробуем получить по email из логина
      // Но без email мы не можем, поэтому отправляем на логин
      this.router.navigate(['/login']);
      this.loading = false;
    }
  }

  logout(): void {
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}