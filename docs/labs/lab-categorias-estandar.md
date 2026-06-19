# Actividad Práctica: Estandarizar Categorías

Fecha de referencia: 2026-05-17

## Objetivo

Refactorizar el módulo simple de categorías para que siga el mismo estándar usado en productos:

- No usar `HttpClient` directamente dentro del componente.
- Crear un servicio de datos.
- Usar interfaces tipadas.
- Consumir el gateway mediante `ApiService`.
- Mantener una plantilla consistente con formulario, tabla y acciones.

## Punto de Partida

El componente `Categorias` inicialmente hacía llamadas HTTP directas:

```ts
constructor(private http: HttpClient) {}

cargarCategorias() {
  this.http.get<any[]>('http://localhost:8090/api/v1/categorias')
    .subscribe(data => this.categorias.set(data));
}
```

Eso funciona para aprender, pero a medida que crece la aplicación conviene separar responsabilidades.

## Paso 1: Crear el Servicio de Categorías

Archivo:

```text
src/app/categorias/categorias.service.ts
```

Responsabilidades:

- Definir el contrato `Categoria`.
- Definir el contrato `CategoriaRequest`.
- Centralizar las operaciones HTTP.
- Usar `ApiService` para construir URLs.

Estructura esperada:

```ts
export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion: string;
}
```

Métodos del servicio:

```ts
listar(): Observable<Categoria[]>
crear(categoria: CategoriaRequest): Observable<Categoria>
actualizar(id: number, categoria: CategoriaRequest): Observable<Categoria>
eliminar(id: number): Observable<void>
```

## Paso 2: Usar el Gateway Desde `ApiService`

En vez de escribir la URL completa:

```ts
http://localhost:8090/api/v1/categorias
```

se usa:

```ts
this.api.buildUrl('/api/v1/categorias')
```

Ventaja:

- Si cambia la URL del gateway, se modifica solo `environment.ts`.
- El componente queda libre de detalles de infraestructura.

## Paso 3: Limpiar el Componente

Archivo:

```text
src/app/categorias/categorias.ts
```

Antes:

- El componente tenía `HttpClient`.
- El componente conocía la URL exacta.
- Se usaba `any[]`.

Después:

- El componente usa `CategoriasService`.
- Usa `signal<Categoria[]>([])`.
- Maneja estado simple: `loading`, `error`.
- Maneja formulario: `formNombre`, `formDescripcion`, `editandoId`.

Estados principales:

```ts
categorias = signal<Categoria[]>([]);
loading = signal(false);
error = signal('');
```

## Paso 4: Unificar el Guardado

En vez de tener un flujo separado en el HTML:

```html
(ngSubmit)="editandoId ? guardarEdicion() : crearCategoria()"
```

se usa un solo método:

```html
(ngSubmit)="guardarCategoria()"
```

Dentro del componente se decide:

- Si `editandoId` tiene valor, se actualiza.
- Si `editandoId` es `null`, se crea.

## Paso 5: Crear el Formulario Estándar

Archivo:

```text
src/app/categorias/categorias.html
```

Formulario esperado:

```html
<form class="categoria-form" (ngSubmit)="guardarCategoria()">
  <input
    type="text"
    name="nombre"
    placeholder="Nombre"
    [(ngModel)]="formNombre"
    required
  >

  <input
    type="text"
    name="descripcion"
    placeholder="Descripción"
    [(ngModel)]="formDescripcion"
    required
  >

  <button type="submit">{{ editandoId ? 'Guardar' : 'Agregar' }}</button>
  <button *ngIf="editandoId" type="button" class="secondary" (click)="cancelarEdicion()">Cancelar</button>
</form>
```

## Paso 6: Crear la Tabla Estándar

La tabla debe mostrar:

- ID.
- Nombre.
- Descripción.
- Acciones.

Acciones:

- Editar.
- Eliminar.

Ejemplo:

```html
<tr *ngFor="let categoria of categorias()">
  <td>{{ categoria.id }}</td>
  <td>{{ categoria.nombre }}</td>
  <td>{{ categoria.descripcion }}</td>
  <td class="actions">
    <button type="button" class="secondary" (click)="iniciarEdicion(categoria)">Editar</button>
    <button type="button" class="danger" (click)="eliminarCategoria(categoria.id)">Eliminar</button>
  </td>
</tr>
```

## Paso 7: Alinear Estilos con Productos

Archivo:

```text
src/app/categorias/categorias.scss
```

Se agregan estilos equivalentes a productos:

- `.categorias-page`
- `.page-title`
- `.categoria-form`
- `.actions`
- botones primarios, secundarios y de peligro
- tabla con bordes
- responsive para pantallas pequeñas

## Resultado Esperado

Al terminar, categorías y productos deben tener el mismo patrón:

```text
Componente -> maneja estado y eventos de UI
Servicio   -> maneja HTTP
ApiService -> construye URLs hacia gateway
HTML       -> muestra formulario y tabla
SCSS       -> mantiene estilo consistente
```

## Checklist

- Existe `categorias.service.ts`.
- El componente ya no importa `HttpClient`.
- El componente usa `CategoriasService`.
- No se usa `any[]` para categorías.
- El formulario crea y edita desde `guardarCategoria()`.
- La tabla permite editar y eliminar.
- Las URLs pasan por `ApiService`.
- El diseño se parece al de productos.

## Preguntas para el Alumno

1. ¿Por qué conviene sacar `HttpClient` del componente?
2. ¿Qué diferencia hay entre `Categoria` y `CategoriaRequest`?
3. ¿Para qué sirve `ApiService`?
4. ¿Por qué `guardarCategoria()` puede crear o actualizar?
5. ¿Qué parte del código cambiaría si el gateway pasa de `7091` a otro puerto?

## Siguiente Mejora

Reutilizar el listado de categorías en productos para elegir la categoría desde un `<select>`, evitando que el usuario escriba manualmente el `idCategoria`.

Estado actual: esta mejora ya fue aplicada en productos. El formulario de productos carga categorías desde `/api/v1/categorias` y usa un `<select>` para seleccionar la categoría.

## Nota de Seguridad

En esta etapa, categorías queda como recurso público porque el gateway permite:

```java
.pathMatchers("/api/v1/categorias/**", "/api/v1/ordenes/**", "/api/v1/pagos/**").permitAll()
```

Por eso no se agrega `authGuard` a `/categorias`.

Productos sí queda protegido:

```ts
{
  path: 'productos',
  canActivate: [authGuard],
  loadComponent: () => import('./productos/productos').then(m => m.Productos),
}
```

Esta separación permite explicar claramente:

- Recurso público: categorías.
- Recurso protegido: productos.
- Autenticación actual: JWT propio emitido por `services/auth-ms`.
- Evolución futura: Keycloak/OAuth2/OIDC.
