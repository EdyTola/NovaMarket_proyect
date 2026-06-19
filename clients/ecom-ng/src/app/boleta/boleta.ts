import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VentaResponse } from '../core/services/ventas.service';

@Component({
  selector: 'app-boleta',
  imports: [CommonModule],
  templateUrl: './boleta.html',
  styleUrl: './boleta.scss',
})
export class Boleta {
  venta = input.required<VentaResponse>();

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

  imprimir() {
    window.print();
  }
}
