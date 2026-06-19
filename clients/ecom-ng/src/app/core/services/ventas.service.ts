import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export type MedioPago = 'EFECTIVO' | 'TARJETA' | 'YAPE';

export interface VentaItemRequest {
  productoId: number;
  cantidad: number;
}

export interface CrearVentaRequest {
  clienteId?: number | null;
  cajeroUsername: string;
  descuento?: number;
  medioPago: MedioPago;
  montoRecibido?: number;
  items: VentaItemRequest[];
}

export interface VentaDetalleResponse {
  id: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface VentaResponse {
  id: number;
  clienteId?: number;
  cajeroUsername: string;
  subtotal: number;
  descuento: number;
  total: number;
  estado: string;
  medioPago?: MedioPago;
  montoRecibido?: number;
  vuelto?: number;
  numeroBoleta?: string;
  fechaVenta?: string;
  pagoId?: number;
  items: VentaDetalleResponse[];
}

@Injectable({ providedIn: 'root' })
export class VentasService {
  constructor(
    private http: HttpClient,
    private api: ApiService,
  ) {}

  crear(venta: CrearVentaRequest): Observable<VentaResponse> {
    return this.http.post<VentaResponse>(this.api.buildUrl('/api/v1/ventas'), venta);
  }

  listar(): Observable<VentaResponse[]> {
    return this.http.get<VentaResponse[]>(this.api.buildUrl('/api/v1/ventas'));
  }

  obtener(id: number): Observable<VentaResponse> {
    return this.http.get<VentaResponse>(this.api.buildUrl(`/api/v1/ventas/${id}`));
  }
}
