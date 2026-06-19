import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Producto, ProductosService } from '../productos/productos.service';

@Component({
  selector: 'app-inventario',
  imports: [CommonModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.scss',
})
export class Inventario {
  alertas = signal<Producto[]>([]);
  loading = signal(false);
  error = signal('');

  constructor(private productosService: ProductosService) {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.productosService.alertasStockBajo().subscribe({
      next: productos => {
        this.alertas.set(productos);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las alertas');
        this.loading.set(false);
      },
    });
  }
}
