import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notification/notification.service';
import { formatBrl } from '../../../../shared/pipes/format-brl';
import { Beneficio } from '../../models/beneficio.model';
import { TransferRequest } from '../../models/transfer-request.model';
import { BeneficioService } from '../../services/beneficio.service';
import { BeneficioFormComponent } from '../beneficio-form/beneficio-form.component';
import { BeneficioTableComponent } from '../beneficio-table/beneficio-table.component';
import { TransferFormComponent } from '../transfer-form/transfer-form.component';

@Component({
  selector: 'app-beneficio-list-page',
  standalone: true,
  imports: [CommonModule, BeneficioFormComponent, BeneficioTableComponent, TransferFormComponent],
  templateUrl: './beneficio-list-page.component.html'
})
export class BeneficioListPageComponent implements OnInit {
  private readonly service = inject(BeneficioService);
  private readonly notifications = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);

  @ViewChild(TransferFormComponent) private transferForm?: TransferFormComponent;

  protected readonly beneficios = signal<Beneficio[]>([]);
  protected readonly editing = signal<Beneficio | null>(null);
  protected readonly nameOptions = computed(() => this.beneficios().map(b => b.nome));

  ngOnInit(): void {
    this.reload();
  }

  protected reload(): void {
    this.service.list().subscribe({
      next: bs => this.beneficios.set(bs),
      error: e => this.notifications.error(this.formatError(e))
    });
  }

  protected onSave(beneficio: Beneficio): void {
    const isEdit = beneficio.id != null;
    const op = isEdit
      ? this.service.update(beneficio.id!, beneficio)
      : this.service.create(beneficio);
    op.subscribe({
      next: () => {
        this.notifications.success(isEdit ? 'Benefício atualizado com sucesso.' : 'Benefício criado com sucesso.');
        this.editing.set(null);
        this.reload();
      },
      error: e => this.notifications.error(this.formatError(e))
    });
  }

  protected onCancel(): void {
    this.editing.set(null);
  }

  protected onEditRequested(b: Beneficio): void {
    this.confirmation.ask({
      title: 'Editar benefício',
      message: `Carregar "${b.nome}" no formulário para edição?`,
      confirmLabel: 'Editar',
      onConfirm: () => this.editing.set(b)
    });
  }

  protected onDeleteRequested(b: Beneficio): void {
    this.confirmation.ask({
      title: 'Excluir benefício',
      message: `Tem certeza que deseja excluir "${b.nome}" (id ${b.id})? Esta operação não pode ser desfeita.`,
      confirmLabel: 'Excluir',
      danger: true,
      onConfirm: () => this.deleteBeneficio(b)
    });
  }

  protected onTransferRequested(req: TransferRequest): void {
    const fromLabel = req.fromId != null ? `id ${req.fromId}` : `"${req.fromName}"`;
    const toLabel = req.toId != null ? `id ${req.toId}` : `"${req.toName}"`;
    this.confirmation.ask({
      title: 'Confirmar transferência',
      message: `Transferir ${formatBrl(req.amount)} de ${fromLabel} para ${toLabel}?`,
      confirmLabel: 'Transferir',
      onConfirm: () => this.executeTransfer(req)
    });
  }

  private deleteBeneficio(b: Beneficio): void {
    if (b.id == null) return;
    this.service.delete(b.id).subscribe({
      next: () => {
        this.notifications.success(`Benefício "${b.nome}" excluído com sucesso.`);
        this.reload();
      },
      error: e => this.notifications.error(this.formatError(e))
    });
  }

  private executeTransfer(req: TransferRequest): void {
    this.service.transfer(req).subscribe({
      next: () => {
        this.notifications.success(`Transferência de ${formatBrl(req.amount)} realizada com sucesso.`);
        this.transferForm?.reset();
        this.reload();
      },
      error: e => this.notifications.error(this.formatError(e))
    });
  }

  private formatError(e: any): string {
    return e?.error?.message ?? e?.message ?? 'erro desconhecido';
  }
}
