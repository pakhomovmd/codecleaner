import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProjectListComponent } from './components/project-list/project-list.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ProjectDetailComponent } from './components/project-detail/project-detail.component';
import { AnalysisDetailComponent } from './components/analysis-detail/analysis-detail.component';
import { AdminPanelComponent } from './components/admin-panel/admin-panel.component';

export const routes: Routes = [
    { path: '', redirectTo: '/projects', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'projects', component: ProjectListComponent },
    { path: 'profile', component: ProfileComponent },
    { path: 'project/:id', component: ProjectDetailComponent },
    { path: 'analysis/:id', component: AnalysisDetailComponent },
    { path: 'admin', component: AdminPanelComponent }
];