import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjectService, Project } from '../../services/project.service';
import { AnalysisService, AnalysisMethod } from '../../services/analysis.service';

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
  analyzingCloned = false;
  
  // Методы анализа
  analysisMethods: AnalysisMethod[] = [];
  selectedMethod: string = 'SIMPLE_TEXT_SEARCH';
  
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
        this.loadAnalysisMethods();
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

  loadAnalysisMethods(): void {
    this.analysisService.getAnalysisMethods().subscribe({
      next: (data) => {
        this.analysisMethods = data;
      },
      error: (err) => console.error('Error loading analysis methods:', err)
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
    
    this.analysisService.uploadAndAnalyze(this.projectId!, this.selectedFile, this.selectedMethod).subscribe({
      next: (response) => {
        this.uploadStatus = 'Анализ завершён!';
        this.uploading = false;
        this.selectedFile = null;
        // Обновляем список анализов через 2 секунды
        setTimeout(() => this.loadAnalyses(), 2000);
      },
      error: (err) => {
        console.error(err);
        this.uploadStatus = 'Ошибка: ' + (err.error?.error || err.message);
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

  getMethodDisplayName(methodName: string): string {
    const method = this.analysisMethods.find(m => m.name === methodName);
    return method ? method.displayName : methodName;
  }

  getSelectedMethodDescription(): string {
    const method = this.analysisMethods.find(m => m.name === this.selectedMethod);
    return method ? method.description : '';
  }

  deleteAnalysis(event: Event, analysisId: number): void {
    event.stopPropagation();
    
    if (confirm('Вы уверены, что хотите удалить этот анализ?')) {
      this.analysisService.deleteAnalysis(analysisId).subscribe({
        next: () => {
          console.log('Analysis deleted successfully');
          this.loadAnalyses();
        },
        error: (err) => {
          console.error('Error deleting analysis:', err);
          alert('Ошибка при удалении анализа: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  deleteAllAnalyses(): void {
    if (confirm('Вы уверены, что хотите удалить ВСЮ историю анализов? Это действие нельзя отменить.')) {
      this.analysisService.deleteAllAnalysesByProject(this.projectId!).subscribe({
        next: () => {
          console.log('All analyses deleted successfully');
          this.loadAnalyses();
        },
        error: (err) => {
          console.error('Error deleting all analyses:', err);
          alert('Ошибка при удалении истории: ' + (err.error?.message || err.message));
        }
      });
    }
  }
  
  analyzeClonedRepo(): void {
    if (!this.project?.clonedZipPath) {
      alert('Клонированный репозиторий не найден');
      return;
    }
    
    this.analyzingCloned = true;
    this.uploadStatus = 'Анализ клонированного репозитория...';
    
    this.analysisService.analyzeClonedRepository(this.projectId!, this.selectedMethod).subscribe({
      next: (response) => {
        this.uploadStatus = 'Анализ завершён!';
        this.analyzingCloned = false;
        alert('Анализ клонированного репозитория завершён!');
        // Обновляем список анализов через 2 секунды
        setTimeout(() => this.loadAnalyses(), 2000);
      },
      error: (err) => {
        console.error(err);
        this.uploadStatus = 'Ошибка: ' + (err.error?.error || err.message);
        this.analyzingCloned = false;
        alert('Ошибка при анализе: ' + (err.error?.error || err.message));
      }
    });
  }
}