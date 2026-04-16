import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjectService, Project } from '../../services/project.service';
import { AnalysisService } from '../../services/analysis.service';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.css']
})
export class ProjectDetailComponent implements OnInit {
  projectId: number | null = null;
  project: Project | null = null;
  loading = true;
  
  // Для загрузки файла
  selectedFile: File | null = null;
  uploadStatus = '';
  uploading = false;
  
  // Результаты анализов
  analyses: any[] = [];
  selectedAnalysis: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projectId = +params['id'];
      if (this.projectId) {
        this.loadProject();
        this.loadAnalyses();
      }
    });
  }

  loadProject(): void {
    this.loading = true;
    this.projectService.getProject(this.projectId!).subscribe({
      next: (data) => {
        this.project = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadAnalyses(): void {
    this.analysisService.getAnalysesByProject(this.projectId!).subscribe({
      next: (data) => {
        this.analyses = data;
      },
      error: (err) => console.error(err)
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
    this.uploadStatus = '';
  }

  uploadAndAnalyze(): void {
    if (!this.selectedFile) {
      this.uploadStatus = 'Выберите ZIP-архив с проектом';
      return;
    }
    
    this.uploading = true;
    this.uploadStatus = 'Загрузка и анализ...';
    
    this.analysisService.uploadAndAnalyze(this.projectId!, this.selectedFile).subscribe({
      next: (response) => {
        this.uploadStatus = 'Анализ запущен!';
        this.uploading = false;
        this.selectedFile = null;
        // Обновляем список анализов через 2 секунды
        setTimeout(() => this.loadAnalyses(), 2000);
      },
      error: (err) => {
        console.error(err);
        this.uploadStatus = 'Ошибка: ' + (err.error || err.message);
        this.uploading = false;
      }
    });
  }

viewAnalysis(analysisId: number): void {
  this.router.navigate(['/analysis', analysisId]);
}

  goBack(): void {
    this.router.navigate(['/projects']);
  }
}