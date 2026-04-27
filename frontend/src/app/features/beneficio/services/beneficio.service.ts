import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Beneficio } from '../models/beneficio.model';
import { TransferRequest } from '../models/transfer-request.model';

@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api/v1/beneficios';

  list(): Observable<Beneficio[]> {
    return this.http.get<Beneficio[]>(this.base);
  }

  get(id: number): Observable<Beneficio> {
    return this.http.get<Beneficio>(`${this.base}/${id}`);
  }

  create(b: Beneficio): Observable<Beneficio> {
    return this.http.post<Beneficio>(this.base, b);
  }

  update(id: number, b: Beneficio): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${this.base}/${id}`, b);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  transfer(req: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/transfer`, req);
  }
}
