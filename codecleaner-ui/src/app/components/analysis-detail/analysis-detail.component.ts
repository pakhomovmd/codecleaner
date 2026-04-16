import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AnalysisService } from '../../services/analysis.service';

@Component({
  selector: 'app-analysis-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './analysis-detail.component.html',
  styleUrls: ['./analysis-detail.component.css']
})
export class AnalysisDetailComponent implements OnInit {
  sessionId: number | null = null;
  analysis: any = null;
  loading = true;
  selectedFile: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.sessionId = +params['id'];
      if (this.sessionId) {
        this.loadAnalysisDetails();
      }
    });
  }

  loadAnalysisDetails(): void {
    this.loading = true;
    this.analysisService.getAnalysisDetails(this.sessionId!).subscribe({
      next: (data) => {
        this.analysis = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading analysis details:', err);
        this.loading = false;
      }
    });
  }

  viewFileDetails(file: any): void {
    this.selectedFile = file;
  }

  closeFileDetails(): void {
    this.selectedFile = null;
  }

  getFileTypeIcon(fileType: string): string {
    return fileType === 'CSS' ? '🎨' : '📜';
  }

  goBack(): void {
    this.router.navigate(['/project', this.analysis?.project?.id]);
  }
}