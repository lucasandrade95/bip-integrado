import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { TransferRequest } from '../../models/transfer-request.model';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxMaskDirective],
  templateUrl: './transfer-form.component.html',
  styleUrls: ['./transfer-form.component.css']
})
export class TransferFormComponent {
  @Input() nameOptions: string[] = [];

  @Output() readonly transferRequested = new EventEmitter<TransferRequest>();

  protected fromId: number | null = null;
  protected fromName = '';
  protected toId: number | null = null;
  protected toName = '';
  protected amount: number | null = null;

  protected canSubmit(): boolean {
    const hasFrom = this.fromId != null || this.fromName.trim().length > 0;
    const hasTo = this.toId != null || this.toName.trim().length > 0;
    const hasAmount = this.amount != null && this.amount > 0;
    return hasFrom && hasTo && hasAmount;
  }

  protected submit(): void {
    if (!this.canSubmit()) return;
    const req: TransferRequest = { amount: this.amount! };
    if (this.fromId != null) {
      req.fromId = this.fromId;
    } else if (this.fromName.trim()) {
      req.fromName = this.fromName.trim();
    }
    if (this.toId != null) {
      req.toId = this.toId;
    } else if (this.toName.trim()) {
      req.toName = this.toName.trim();
    }
    this.transferRequested.emit(req);
  }

  reset(): void {
    this.fromId = null;
    this.fromName = '';
    this.toId = null;
    this.toName = '';
    this.amount = null;
  }
}
