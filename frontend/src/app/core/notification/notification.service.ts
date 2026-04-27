import { Injectable, signal } from '@angular/core';
import { Notification, NotificationKind } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly current = signal<Notification | null>(null);
  readonly value = this.current.asReadonly();

  notify(kind: NotificationKind, text: string, autoDismissMs = 3500): void {
    this.current.set({ kind, text });
    if (autoDismissMs > 0) {
      setTimeout(() => {
        if (this.current()?.text === text) {
          this.current.set(null);
        }
      }, autoDismissMs);
    }
  }

  success(text: string): void {
    this.notify('success', text);
  }

  error(text: string): void {
    this.notify('error', text);
  }

  dismiss(): void {
    this.current.set(null);
  }
}
