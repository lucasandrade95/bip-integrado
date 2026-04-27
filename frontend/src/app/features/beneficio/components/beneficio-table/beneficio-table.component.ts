import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Beneficio } from '../../models/beneficio.model';

@Component({
  selector: 'app-beneficio-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './beneficio-table.component.html',
  styleUrls: ['./beneficio-table.component.css']
})
export class BeneficioTableComponent {
  @Input() beneficios: Beneficio[] = [];

  @Output() readonly editRequested = new EventEmitter<Beneficio>();
  @Output() readonly deleteRequested = new EventEmitter<Beneficio>();

  protected trackById(_i: number, b: Beneficio) {
    return b.id;
  }
}
