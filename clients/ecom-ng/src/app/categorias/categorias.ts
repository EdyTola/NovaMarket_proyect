import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Categoria, CategoriaRequest, CategoriasService } from './categorias.service';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss',
})
export class Categorias {
  categorias = signal<Categoria[]>([]);
  loading = signal(false);
  error = signal('');
  formNombre = '';
  formDescripcion = '';
  editandoId: number | null = null;

  constructor(private categoriasService: CategoriasService) {
    this.cargarCategorias();
  }

  cargarCategorias() {
    this.loading.set(true);
    this.error.set('');

    this.categoriasService.listar()
      .subscribe({
        next: (categorias) => {
          console.log('Categorias recibidas:', categorias);
          this.categorias.set(categorias)

        },
        complete: () => this.loading.set(false),
        error: (err) => {
          console.error('Error al cargar categorias:', err);
          this.error.set('No se pudieron cargar las categorías ');
          //this.loading.set(false);
        },
      });
  }

  guardarCategoria() {
    const categoria = this.obtenerCategoriaDesdeFormulario();
    if (!categoria) return;

    if (this.editandoId != null) {
      this.categoriasService.actualizar(this.editandoId, categoria)
        .subscribe({
          next: () => {
            this.cancelarEdicion();
            this.cargarCategorias();
          },
          error: () => this.error.set('No se pudo actualizar la categoría'),
        });
      return;
    }

    this.categoriasService.crear(categoria)
      .subscribe({
        next: () => {
          this.limpiarFormulario();
          this.cargarCategorias();
        },
        error: () => this.error.set('No se pudo crear la categoría'),
      });
  }

  eliminarCategoria(id: number) {
    if (!confirm(`¿Está seguro de eliminar la categoría ${id}?`)) return;

    this.categoriasService.eliminar(id)
      .subscribe({
        next: () => this.cargarCategorias(),
        error: () => this.error.set('No se pudo eliminar la categoría'),
      });
  }

  iniciarEdicion(categoria: Categoria) {
    this.editandoId = categoria.id;
    this.formNombre = categoria.nombre;
    this.formDescripcion = categoria.descripcion;
  }

  cancelarEdicion() {
    this.editandoId = null;
    this.limpiarFormulario();
  }

  private obtenerCategoriaDesdeFormulario(): CategoriaRequest | null {
    const nombre = this.formNombre.trim();
    const descripcion = this.formDescripcion.trim();

    if (!nombre || !descripcion) {
      this.error.set('Nombre y descripción son obligatorios');
      return null;
    }

    this.error.set('');
    return { nombre, descripcion };
  }

  private limpiarFormulario() {
    this.formNombre = '';
    this.formDescripcion = '';
  }
}
