import { Component, OnInit } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MarkdownModule } from 'ngx-markdown';
import { ChangelogService } from '../../services/changelog.service';

@Component({
  selector: 'app-changelog-modal',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatProgressSpinnerModule, MarkdownModule],
  templateUrl: './changelog-modal.component.html',
  styleUrl: './changelog-modal.component.css'
})
export class ChangelogModalComponent implements OnInit {
  readonly title = 'Was ist neu?';
  readonly closeButtonLabel = 'Schliessen';

  isLoading = true;
  hasError = false;
  changelogMarkdown = '';

  constructor(
    private changelogService: ChangelogService,
    private dialogRef: MatDialogRef<ChangelogModalComponent>
  ) {}

  ngOnInit(): void {
    this.changelogService.getLatestAsMarkdown().subscribe({
      next: markdown => {
        this.changelogMarkdown = markdown;
        this.isLoading = false;
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
