import { Injectable, computed, signal } from '@angular/core';

export interface CartLine {
  productoId: number;
  nombre: string;
  precio: number;
  stock: number;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class PosCartService {
  private readonly lines = signal<CartLine[]>([]);

  readonly items = this.lines.asReadonly();

  readonly subtotal = computed(() =>
    this.lines().reduce((sum, line) => sum + line.precio * line.cantidad, 0),
  );

  readonly itemCount = computed(() =>
    this.lines().reduce((sum, line) => sum + line.cantidad, 0),
  );

  addProduct(producto: { id: number; nombre: string; precio: number; stock: number }, cantidad = 1) {
    const existing = this.lines().find(line => line.productoId === producto.id);
    if (existing) {
      const nuevaCantidad = existing.cantidad + cantidad;
      if (nuevaCantidad > producto.stock) {
        throw new Error(`Stock insuficiente. Disponible: ${producto.stock}`);
      }
      this.lines.update(lines =>
        lines.map(line =>
          line.productoId === producto.id ? { ...line, cantidad: nuevaCantidad } : line,
        ),
      );
      return;
    }

    if (cantidad > producto.stock) {
      throw new Error(`Stock insuficiente. Disponible: ${producto.stock}`);
    }

    this.lines.update(lines => [
      ...lines,
      {
        productoId: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        stock: producto.stock,
        cantidad,
      },
    ]);
  }

  updateCantidad(productoId: number, cantidad: number) {
    if (cantidad < 1) {
      this.remove(productoId);
      return;
    }

    this.lines.update(lines =>
      lines.map(line => {
        if (line.productoId !== productoId) {
          return line;
        }
        if (cantidad > line.stock) {
          throw new Error(`Stock insuficiente. Disponible: ${line.stock}`);
        }
        return { ...line, cantidad };
      }),
    );
  }

  remove(productoId: number) {
    this.lines.update(lines => lines.filter(line => line.productoId !== productoId));
  }

  clear() {
    this.lines.set([]);
  }
}
