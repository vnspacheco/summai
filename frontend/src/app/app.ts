import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { marked } from 'marked';
import { SummarizerService } from './summarizer.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly formBuilder = inject(FormBuilder);
  private readonly summarizerService = inject(SummarizerService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly destroyRef = inject(DestroyRef);

  private readonly loadingMessages = [
    'Validando o link do YouTube...',
    'Buscando transcrição...',
    'Gerando resumo com IA...'
  ];

  private loadingTimer?: number;
  private copyFeedbackTimer?: number;

  readonly youtubePattern =
    /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=[\w-]{11}.*|youtu\.be\/[\w-]{11}.*|youtube\.com\/shorts\/[\w-]{11}.*)$/i;

  readonly form = this.formBuilder.nonNullable.group({
    videoUrl: ['', [Validators.required, Validators.pattern(this.youtubePattern)]]
  });

  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly summary = signal('');
  readonly statusMessage = signal(this.loadingMessages[0]);
  readonly copied = signal(false);

  readonly renderedSummary = computed(() =>
    this.sanitizer.bypassSecurityTrustHtml(marked.parse(this.summary()) as string)
  );

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.stopLoadingMessages();
      window.clearTimeout(this.copyFeedbackTimer);
    });
  }

  get videoUrlControl() {
    return this.form.controls.videoUrl;
  }

  submit(): void {
    if (this.form.invalid || this.isLoading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.summary.set('');
    this.copied.set(false);
    this.isLoading.set(true);
    this.startLoadingMessages();

    this.summarizerService
      .summarize(this.videoUrlControl.getRawValue().trim())
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.stopLoadingMessages();
        })
      )
      .subscribe({
        next: (response) => {
          this.summary.set(response.summary);
        },
        error: (error) => {
          this.errorMessage.set(
            error?.error?.error ?? 'Não foi possível resumir este vídeo. Tente novamente.'
          );
        }
      });
  }

  async copySummary(): Promise<void> {
    if (!this.summary()) {
      return;
    }

    try {
      await navigator.clipboard.writeText(this.summary());
      this.copied.set(true);
      window.clearTimeout(this.copyFeedbackTimer);
      this.copyFeedbackTimer = window.setTimeout(() => this.copied.set(false), 2500);
    } catch {
      this.errorMessage.set('Não foi possível copiar o texto automaticamente.');
    }
  }

  downloadSummary(): void {
    if (!this.summary()) {
      return;
    }

    const blob = new Blob([this.summary()], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = 'resumo-youtube.md';
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private startLoadingMessages(): void {
    let index = 0;
    this.statusMessage.set(this.loadingMessages[index]);
    this.stopLoadingMessages();
    this.loadingTimer = window.setInterval(() => {
      index = (index + 1) % this.loadingMessages.length;
      this.statusMessage.set(this.loadingMessages[index]);
    }, 2200);
  }

  private stopLoadingMessages(): void {
    window.clearInterval(this.loadingTimer);
  }
}
