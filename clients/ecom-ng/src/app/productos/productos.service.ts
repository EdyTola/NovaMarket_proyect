import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../core/services/api.service';

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  idCategoria: number;
  precio?: number;
  stock?: number;
  stockMinimo?: number;
  codigoBarras?: string;
  imagenUrl?: string;
  stockBajo?: boolean;
}

export interface Categoria {
  id: number;
  nombre: string;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  idCategoria: number;
  precio: number;
  stock?: number;
  stockMinimo?: number;
  codigoBarras?: string;
  imagenUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductosService {
  constructor(
    private http: HttpClient,
    private api: ApiService,
  ) {}

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.api.buildUrl('/api/v1/productos'));
  }

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.api.buildUrl('/api/v1/categorias'));
  }

  crear(producto: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.api.buildUrl('/api/v1/productos'), producto);
  }

  actualizar(id: number, producto: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(this.api.buildUrl(`/api/v1/productos/${id}`), producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`/api/v1/productos/${id}`));
  }

  buscarPorCodigo(codigoBarras: string): Observable<Producto> {
    return this.http.get<Producto>(this.api.buildUrl(`/api/v1/productos/codigo/${encodeURIComponent(codigoBarras)}`));
  }

  alertasStockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.api.buildUrl('/api/v1/productos/alertas/stock-bajo'));
  }
}
