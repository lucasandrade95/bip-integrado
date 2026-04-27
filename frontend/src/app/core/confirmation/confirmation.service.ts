import { Injectable, signal } from '@angular/core';
import { PendingAction } from './confirmation.model';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
  private readonly pending = signal<PendingAction | null>(null);
  readonly value = this.pending.asReadonly();

  ask(action: PendingAction): void {
    this.pending.set(action);
  }

  confirm(): void {
    const action = this.pending();
    if (!action) return;
    this.pending.set(null);
    action.onConfirm();
  }

  cancel(): void {
    this.pending.set(null);
  }
}
