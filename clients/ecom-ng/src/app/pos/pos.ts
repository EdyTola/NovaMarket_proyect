import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/auth/auth.service';
import { Boleta } from '../boleta/boleta';
import { Cliente, ClientesService } from '../core/services/clientes.service';
import { MedioPago, VentaResponse, VentasService } from '../core/services/ventas.service';
import { Producto, ProductosService } from '../productos/productos.service';
import { PosCartService } from './pos-cart.service';

@Component({
  selector: 'app-pos',
  imports: [CommonModule, FormsModule, Boleta],
  templateUrl: './pos.html',
  styleUrl: './pos.scss',
})
export class Pos {
  private readonly cart = inject(PosCartService);
  private readonly productosService = inject(ProductosService);
  private readonly ventasService = inject(VentasService);
  private readonly clientesService = inject(ClientesService);
  protected readonly auth = inject(AuthService);

  readonly cartItems = this.cart.items;
  readonly subtotal = this.cart.subtotal;
  readonly itemCount = this.cart.itemCount;

  readonly mediosPago: { value: MedioPago; label: string }[] = [
    { value: 'EFECTIVO', label: 'Efectivo' },
    { value: 'TARJETA', label: 'Tarjeta' },
    { value: 'YAPE', label: 'Yape' },
  ];

  codigoBarras = '';
  descuento = 0;
  clienteId: number | null = null;
  medioPago: MedioPago = 'EFECTIVO';
  montoRecibido: number | null = null;

  clientes = signal<Cliente[]>([]);
  ventaCompletada = signal<VentaResponse | null>(null);
  loading = signal(false);
  error = signal('');

  total = computed(() => Math.max(0, this.subtotal() - (this.descuento || 0)));

  vuelto = computed(() => {
    if (this.medioPago !== 'EFECTIVO') {
      return 0;
    }
    const recibido = Number(this.montoRecibido ?? 0);
    return Math.max(0, recibido - this.total());
  });

  puedeCobrar = computed(() => {
    if (!this.cartItems().length) {
      return false;
    }
    if (this.medioPago === 'EFECTIVO') {
      return Number(this.montoRecibido ?? 0) >= this.total();
    }
    return true;
  });

  constructor() {
    this.cargarClientes();
  }

  cargarClientes() {
    this.clientesService.listar().subscribe({
      next: clientes => this.clientes.set(clientes),
      error: () => {},
    });
  }

  buscarPorCodigo() {
    const codigo = this.codigoBarras.trim();
    if (!codigo) {
      return;
    }

    this.error.set('');
    this.loading.set(true);

    this.productosService.buscarPorCodigo(codigo).subscribe({
      next: producto => {
        try {
          this.agregarAlCarrito(producto);
          this.codigoBarras = '';
        } catch (e) {
          this.error.set(e instanceof Error ? e.message : 'No se pudo agregar');
        }
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Artículo no encontrado para ese código');
        this.loading.set(false);
      },
    });
  }

  agregarAlCarrito(producto: Producto) {
    const precio = Number(producto.precio ?? 0);
    const stock = Number(producto.stock ?? 0);
    this.cart.addProduct({
      id: producto.id,
      nombre: producto.nombre,
      precio,
      stock,
    });
  }

  cambiarCantidad(productoId: number, cantidad: number) {
    try {
      this.cart.updateCantidad(productoId, cantidad);
      this.error.set('');
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Cantidad inválida');
    }
  }

  quitar(productoId: number) {
    this.cart.remove(productoId);
  }

  limpiarCarrito() {
    this.cart.clear();
    this.descuento = 0;
    this.montoRecibido = null;
    this.ventaCompletada.set(null);
  }

  onMedioPagoChange() {
    if (this.medioPago !== 'EFECTIVO') {
      this.montoRecibido = this.total();
    } else {
      this.montoRecibido = null;
    }
  }

  confirmarPago() {
    if (!this.puedeCobrar()) {
      this.error.set('Revise el carrito y el monto recibido');
      return;
    }

    const username = this.auth.username();
    if (!username) {
      this.error.set('Debe iniciar sesión');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const payload = {
      clienteId: this.clienteId,
      cajeroUsername: username,
      descuento: this.descuento || 0,
      medioPago: this.medioPago,
      montoRecibido: this.medioPago === 'EFECTIVO' ? Number(this.montoRecibido) : undefined,
      items: this.cartItems().map(line => ({
        productoId: line.productoId,
        cantidad: line.cantidad,
      })),
    };

    this.ventasService.crear(payload).subscribe({
      next: venta => {
        this.ventaCompletada.set(venta);
        this.cart.clear();
        this.descuento = 0;
        this.montoRecibido = null;
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cobrar. Verifique stock y que ms-venta / ms-pago estén activos.');
        this.loading.set(false);
      },
    });
  }

  nuevaVenta() {
    this.ventaCompletada.set(null);
    this.error.set('');
  }

  lineTotal(line: { precio: number; cantidad: number }): number {
    return line.precio * line.cantidad;
  }
}
