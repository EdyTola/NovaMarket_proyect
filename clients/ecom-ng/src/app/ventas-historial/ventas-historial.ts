import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VentaResponse, VentasService } from '../core/services/ventas.service';
import { Boleta } from '../boleta/boleta';

@Component({
  selector: 'app-ventas-historial',
  imports: [CommonModule, RouterLink, Boleta],
  templateUrl: './ventas-historial.html',
  styleUrl: './ventas-historial.scss',
})
export class VentasHistorial {
  ventas = signal<VentaResponse[]>([]);
  seleccionada = signal<VentaResponse | null>(null);
  loading = signal(false);
  error = signal('');

  constructor(private ventasService: VentasService) {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.ventasService.listar().subscribe({
      next: lista => {
        this.ventas.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de ventas');
        this.loading.set(false);
      },
    });
  }

  verBoleta(venta: VentaResponse) {
    this.seleccionada.set(venta);
  }

  cerrarBoleta() {
    this.seleccionada.set(null);
  }

  etiquetaMedio(medio?: string): string {
    switch (medio) {
      case 'EFECTIVO':
        return 'Efectivo';
      case 'TARJETA':
        return 'Tarjeta';
      case 'YAPE':
        return 'Yape';
      default:
        return medio ?? '—';
    }
  }
}
