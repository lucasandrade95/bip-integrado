import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { Beneficio } from '../../models/beneficio.model';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxMaskDirective],
  templateUrl: './beneficio-form.component.html',
  styleUrls: ['./beneficio-form.component.css']
})
export class BeneficioFormComponent {
  @Input() set editing(value: Beneficio | null) {
    this.editingState = value;
    this.form = value ? { ...value } : this.empty();
  }

  @Output() readonly save = new EventEmitter<Beneficio>();
  @Output() readonly cancel = new EventEmitter<void>();

  protected editingState: Beneficio | null = null;
  protected form: Beneficio = this.empty();

  protected get isEdit(): boolean {
    return this.editingState?.id != null;
  }

  protected submit(): void {
    this.save.emit({ ...this.form });
  }

  protected doCancel(): void {
    this.form = this.empty();
    this.cancel.emit();
  }

  private empty(): Beneficio {
    return { nome: '', descricao: '', valor: null, ativo: true };
  }
}
