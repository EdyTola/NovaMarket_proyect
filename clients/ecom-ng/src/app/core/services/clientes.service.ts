import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface Cliente {
  id: number;
  nombre: string;
  documento: string;
  email?: string;
  telefono?: string;
  puntos: number;
}

@Injectable({ providedIn: 'root' })
export class ClientesService {
  constructor(
    private http: HttpClient,
    private api: ApiService,
  ) {}

  listar(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.api.buildUrl('/api/v1/clientes'));
  }
}
