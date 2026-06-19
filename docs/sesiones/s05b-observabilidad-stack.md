# obs

Stack compartido de observabilidad para la plataforma.

Incluye:

- Prometheus
- Loki
- Promtail
- Grafana

Este modulo no levanta Kafka ni microservicios. Solo observa servicios que ya estan disponibles.

## Archivos

- `compose-dev.yml`: observabilidad para `dev`
- `compose.yml`: observabilidad para `prod`
- `prometheus/prometheus-dev.yml`: targets dev por `host.docker.internal`
- `prometheus/prometheus.yml`: targets prod por nombres Docker en `ecom-prod-net`
- `grafana/provisioning`: datasources de Grafana

## Puertos

| Herramienta | DEV | PROD |
|---|---:|---:|
| Prometheus | 19090 | 29090 |
| Loki | 13100 | 23100 |
| Grafana | 13000 | 23000 |

Grafana usa:

```text
admin / admin
```

## Redes

En `dev`, observability usa una red propia:

```text
obs-dev-net
```

Los servicios dev suelen correr en el host con Maven, por eso Prometheus usa `host.docker.internal`.

En `prod`, observability usa la red externa:

```text
ecom-prod-net
```

Esa red la crea `infra`.

## Targets DEV

Prometheus dev scrapea:

- Gateway: `host.docker.internal:8090`
- Producto: `host.docker.internal:9092`
- Catalogo: `host.docker.internal:8082`
- Kafka exporter: `host.docker.internal:41308`
- Orden MS: `host.docker.internal:19051`
- Pago MS: `host.docker.internal:19061`

## Targets PROD

Prometheus prod scrapea:

- Gateway: `gateway:8090`
- Producto: `producto:9092`
- Catalogo: `catalogo:8082`
- Kafka exporter: `kafka-exporter:9308`
- Orden MS: `orden-ms:9021`
- Pago MS: `pago-ms:9031`

## Logs Centralizados

Promtail lee estas carpetas y envia los logs a Loki con etiqueta `service`:

- `infra/gateway/logs` -> `gateway`
- `services/catalogo-ms/logs` -> `catalogo`
- `services/producto-ms/logs` -> `producto`
- `services/orden-ms/logs` -> `orden-ms`
- `services/pago-ms/logs` -> `pago-ms`

## Levantar DEV

Puedes levantar observability antes o despues de Kafka y los microservicios. Si un target aun no existe, Prometheus lo marcara `DOWN` hasta que aparezca.

```powershell
cd obs
docker compose -f compose-dev.yml up -d
```

Accesos:

- Prometheus: `http://localhost:19090`
- Grafana: `http://localhost:13000`
- Loki: `http://localhost:13100`

## Levantar PROD

Primero debe existir `ecom-prod-net`, creada por `infra`:

```powershell
cd infra
docker compose up -d
```

Luego Kafka y microservicios, si quieres ver sus metricas. Finalmente:

```powershell
cd obs
docker compose up -d
```

Accesos:

- Prometheus: `http://localhost:29090`
- Grafana: `http://localhost:23000`
- Loki: `http://localhost:23100`

## Validaciones

En Prometheus, revisar:

```text
Status -> Targets
```

Para Kafka:

- dev: target `kafka-exporter-dev`
- prod: target `kafka-exporter`

Si aparece `UP`, Grafana ya puede consultar esas metricas desde Prometheus.

## Logs

```powershell
# dev
docker compose -f compose-dev.yml logs -f prometheus
docker compose -f compose-dev.yml logs -f grafana

# prod
docker compose logs -f prometheus
docker compose logs -f grafana
```

## Estado de avance

- [x] Prometheus dev/prod
- [x] Grafana dev/prod
- [x] Loki dev/prod
- [x] Promtail para logs centralizados
- [x] Scrape de Gateway, catalogo y producto
- [x] Scrape de `orden-ms` y `pago-ms`
- [x] Scrape de Kafka Exporter
- [x] Datasource Grafana provisionado
- [ ] Dashboards versionados por servicio
- [ ] Alertas formales por ambiente

---

## Tag sugerido

```bash
git tag -a vs09-kafka -m "eda con vs09-kafka"
git push origin vs09-kafka
```
