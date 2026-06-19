import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../core/services/api.service';

export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class CategoriasService {
  constructor(
    private http: HttpClient,
    private api: ApiService,
  ) {}

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.api.buildUrl('/api/v1/categorias'));
  }

  crear(categoria: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.api.buildUrl('/api/v1/categorias'), categoria);
  }

  actualizar(id: number, categoria: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(this.api.buildUrl(`/api/v1/categorias/${id}`), categoria);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`/api/v1/categorias/${id}`));
  }
}
