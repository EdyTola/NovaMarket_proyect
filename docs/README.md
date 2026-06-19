# Libro Digital ecom - Microservicios 2026

Este directorio publica la documentación del proyecto `ecom` como libro digital en Markdown usando MkDocs Material.

## Qué encontrará el estudiante

- Sesiones de arquitectura de microservicios.
- Laboratorios y rúbricas.
- Diagramas Mermaid renderizables en MkDocs.
- Guías para infraestructura, gateway, seguridad, observabilidad y Kafka.

## Enfoque del proyecto

El proyecto construye una plataforma e-commerce educativa con Spring Boot, Spring Cloud, PostgreSQL, Kafka y observabilidad.

## Ruta de aprendizaje

| Bloque | Contenido | Producto esperado |
|---|---:|---|
| Arquitectura base | Config Server, Eureka, Gateway | Plataforma distribuida base |
| Servicios | Auth, Catálogo, Producto, Orden, Pago | Microservicios integrados |
| Transversales | Seguridad, observabilidad, Kafka | Sistema observable y resiliente |

## Ejecución local

### Con Docker

```powershell
docker compose up
```

Luego abra:

```text
http://127.0.0.1:8002/
```

### Con Python local

```powershell
python -m pip install mkdocs mkdocs-material pymdown-extensions
cd ..
mkdocs serve
```

## Generación del sitio

```powershell
docker compose run --rm mkdocs mkdocs build -f mkdocs.yml
```

o bien:

```powershell
mkdocs build
```

## Estructura del libro

- `sesiones/`: documentación por sesión (con diagramas Mermaid embebidos).
- `labs/`: laboratorios y actividades.
