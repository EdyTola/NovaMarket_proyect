# kafka

Cluster Kafka en modo KRaft (sin Zookeeper) con exporter y UI.

## Servicios

| Servicio | Puerto host DEV | Puerto host PROD | Puerto container |
|---|---:|---:|---:|
| Kafka broker | 41092 | 28092 | 9092 |
| Kafka UI | 41085 | 28085 | 8080 |
| Kafka Exporter | 41308 | 29308 | 9308 |

## Inicio rápido

```bash
# DEV (red ecom-dev-net)
docker compose -f compose-dev.yml up -d
#   http://localhost:41085 — Kafka UI

# PROD (red ecom-prod-net)
docker compose up -d
#   http://localhost:28085 — Kafka UI
```

## Operacion basica

Entrar al broker:

```powershell
# DEV
docker compose -f compose-dev.yml exec kafka bash

# PROD
docker compose exec kafka bash
```

Crear topics:

```bash
/opt/kafka/bin/kafka-topics.sh --create \
  --topic orden-eventos \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1

/opt/kafka/bin/kafka-topics.sh --create \
  --topic pago-eventos \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1
```

Listar topics:

```bash
/opt/kafka/bin/kafka-topics.sh --list \
  --bootstrap-server kafka:9092
```

Ver detalle de un topic:

```bash
/opt/kafka/bin/kafka-topics.sh --describe \
  --topic orden-eventos \
  --bootstrap-server kafka:9092
```

Producer manual:

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --topic orden-eventos \
  --bootstrap-server kafka:9092
```

Consumer manual:

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --topic orden-eventos \
  --bootstrap-server kafka:9092 \
  --from-beginning
```

Tambien puedes ejecutar los comandos sin entrar al broker:

```powershell
docker exec -it ecom-kafka-dev /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server kafka:9092

docker exec -it ecom-kafka-dev /opt/kafka/bin/kafka-topics.sh --create --topic orden-eventos --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1

docker exec -it ecom-kafka-dev /opt/kafka/bin/kafka-topics.sh --create --topic pago-eventos --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
```

Documentación en [`../docs/`](../docs/).
