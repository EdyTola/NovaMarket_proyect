# Sesion 07g2 - Observabilidad con Herramientas en 30 minutos

Esta version es una sesion corta. El objetivo no es construir muchos dashboards, sino quedarse con los casos mas importantes para entender observabilidad:

- metricas para detectar que esta pasando
- logs para explicar por que paso
- una alerta simple para avisar cuando algo se cae

Herramientas:

- Prometheus -> metricas
- Loki -> logs
- Promtail -> envio de logs
- Grafana -> dashboards, exploracion y alerta

---

## Objetivo de la clase

Al final de la sesion, el alumno debe poder explicar:

1. Si los servicios estan vivos.
2. Si hay trafico HTTP.
3. Si hay errores.
4. Que dicen los logs cuando una peticion falla.
5. Como configurar una alerta basica en Grafana.

Flujo observado:

```text
Cliente -> Gateway -> Producto -> Catalogo
                       |           |
                       +---- logs y metricas ----+
                                                 |
                         Prometheus + Loki + Grafana
```

---

## Agenda sugerida para 30 minutos

| Tiempo | Actividad | Evidencia |
|---:|---|---|
| 0-5 min | Validar Prometheus y Grafana | targets `UP` |
| 5-12 min | Crear dashboard minimo de metricas | disponibilidad, trafico y errores |
| 12-20 min | Revisar logs en Loki | logs por servicio y `traceId` |
| 20-27 min | Crear una alerta simple | alerta por `catalogo-dev` caido |
| 27-30 min | Cierre | explicar metricas vs logs vs alerta |

---

## Preparacion rapida en DEV

La plataforma base debe estar levantada:

- config
- registry-server
- gateway
- mysql de `catalogo`
- mysql de `producto`
- catalogo
- producto

Luego levantar observabilidad:

```bash
cd obs
docker compose -f compose-dev.yml up -d
```

Como explicar el comando:

```text
cd obs
```

entra a la carpeta donde esta el stack de observabilidad.

```text
docker compose -f compose-dev.yml up -d
```

significa:

- `docker compose` ejecuta los servicios definidos en un archivo compose
- `-f compose-dev.yml` indica que se usara el archivo de desarrollo
- `up` crea y levanta los contenedores
- `-d` los deja corriendo en segundo plano

En esta sesion ese comando levanta:

- Prometheus
- Loki
- Promtail
- Grafana

URLs:

```text
Prometheus: http://localhost:19090
Grafana:    http://localhost:13000
```

Grafana:

```text
usuario: admin
clave:   admin
```

---

## Caso 1. Metricas esenciales

Este caso responde:

```text
Que esta pasando en el sistema?
```

### 1.1 Validar servicios vivos

Abrir:

```text
http://localhost:19090/targets
```

Se espera `UP` en:

- `gateway-dev`
- `producto-dev`
- `catalogo-dev`

Consulta PromQL:

```promql
up
```

Como explicarlo:

- `1` significa que Prometheus pudo consultar el endpoint de metricas.
- `0` significa que Prometheus no pudo consultar el servicio.
- Esto no garantiza que el negocio funcione, solo que el servicio expone metricas.

Lectura de la consulta:

```text
up
```

se lee como:

```text
Prometheus, dime si cada target que tienes configurado esta disponible.
```

Ejemplo:

```text
gateway-dev  = 1 -> Prometheus puede leer metricas del gateway
producto-dev = 1 -> Prometheus puede leer metricas de producto
catalogo-dev = 0 -> Prometheus no puede leer metricas de catalogo
```

Importante:

```text
up no dice si el endpoint de negocio funciona.
up solo dice si Prometheus pudo consultar las metricas.
```

Panel recomendado en Grafana:

- tipo: `Stat`
- datasource: `Prometheus`
- titulo: `Servicios vivos`
- consulta: `up`

---

### 1.2 Generar trafico

Ejecutar varias veces:

```text
http://localhost:8090/api/v1/productos/detalle/1
```

Tambien se puede probar:

```text
http://localhost:8090/api/v1/catalogo/instancia
http://localhost:8090/api/v1/producto/instancia
```

Como explicar estas URLs:

```text
http://localhost:18080/actuator/health
```

es el health del `gateway` en ambiente local.

```text
/api/v1/productos/detalle/1
```

representa una peticion de negocio mas completa: entra por gateway, llega a producto y puede consultar catalogo.

```text
/api/v1/catalogo/instancia
```

sirve para generar trafico hacia catalogo.

```text
/api/v1/producto/instancia
```

sirve para generar trafico hacia producto.

La idea no es probar funcionalidad nueva, sino producir actividad para que Prometheus y Loki tengan algo que mostrar.

---

### 1.3 Ver requests por servicio

Consulta PromQL:

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

Panel recomendado:

- tipo: `Time series`
- datasource: `Prometheus`
- titulo: `Requests por segundo`
- leyenda: `{{job}}`

Como explicarlo:

- muestra que servicios estan recibiendo trafico
- si se entra por gateway, debe moverse `gateway-dev`
- si se consulta detalle de producto, debe moverse `producto-dev`
- si producto llama a catalogo, tambien debe moverse `catalogo-dev`

Explicacion de `[1m]`:

```text
[1m] no significa "mostrar un minuto".
Significa "usar el ultimo minuto de datos para calcular la velocidad del trafico".
```

La metrica `http_server_requests_seconds_count` es un contador acumulado. Siempre va creciendo.

Ejemplo:

```text
Hace 1 minuto: el contador tenia 100 requests
Ahora:         el contador tiene 160 requests
Diferencia:    60 requests
```

Entonces `rate(...[1m])` calcula:

```text
60 requests / 60 segundos = 1 request por segundo
```

Por eso esta consulta no responde "cuantos requests hubo en total", sino:

```text
cuantas requests por segundo esta recibiendo cada servicio,
calculado con lo que paso durante el ultimo minuto
```

Mensaje clave:

```text
Las metricas muestran el comportamiento medible del sistema.
```

---

### 1.4 Ver errores HTTP

Consulta PromQL:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

Panel recomendado:

- tipo: `Time series`
- datasource: `Prometheus`
- titulo: `Errores 5xx`
- leyenda: `{{job}} - {{status}}`

Como explicarlo:

- normalmente este panel debe estar en cero
- si sube, hay errores de servidor
- este panel detecta el problema, pero no explica la causa

Lectura de la consulta:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

se lee como:

```text
Por cada servicio y por cada codigo HTTP 5xx,
dime cuantas respuestas de error por segundo se estan produciendo,
calculado con lo ocurrido durante el ultimo minuto.
```

Partes importantes:

- `status=~"5.."` filtra solo codigos HTTP que empiezan con `5`
- `500`, `502` y `503` entran en ese filtro
- `rate(...[1m])` calcula errores por segundo usando el ultimo minuto
- `sum by (job, status)` agrupa el resultado por servicio y codigo HTTP

Ejemplo:

```text
producto-dev - 500 = 0.2
```

significa aproximadamente:

```text
producto esta generando 0.2 errores 500 por segundo
```

O dicho mas simple:

```text
en promedio, 1 error cada 5 segundos
```

Mensaje clave:

```text
Prometheus ayuda a detectar sintomas.
```

---

## Caso 2. Logs para explicar una peticion

Este caso responde:

```text
Que paso dentro de los servicios?
```

Abrir Grafana:

```text
http://localhost:13000
```

Ir a:

```text
Menu izquierdo -> Explore
```

Importante:

- usar el `Explore` normal de Grafana
- no entrar por `Drilldown -> Logs`
- si aparece `App not found`, significa que se abrio una app/plugin que no esta instalada
- en ese caso volver a Home y entrar por `Explore`

Dentro de Explore, seleccionar:

```text
Datasource: Loki
```

Usar rango corto:

```text
Last 5 minutes
```

Si no aparece nada, probar con:

```text
Last 15 minutes
```

o generar trafico otra vez desde el navegador.

### 2.1 Ver logs de todos los servicios

Consulta LogQL:

```logql
{service=~"gateway|producto|catalogo"}
```

Como explicarlo:

- Loki permite ver logs centralizados
- ya no se necesita abrir tres consolas distintas
- se puede mirar el flujo completo en una sola pantalla

Lectura de la consulta:

```text
{service=~"gateway|producto|catalogo"}
```

se lee como:

```text
Loki, muestrame logs cuyo label service sea gateway, producto o catalogo.
```

Partes importantes:

- `{ ... }` selecciona logs por etiquetas
- `service` es la etiqueta que Promtail agrega a los logs
- `=~` significa "coincide con esta expresion regular"
- `gateway|producto|catalogo` significa "gateway o producto o catalogo"

Esta consulta sirve para ver el recorrido completo de una peticion.

---

### 2.2 Ver logs por servicio

Antes de filtrar por texto, conviene confirmar que Loki si esta recibiendo logs de cada servicio.

Gateway:

```logql
{service="gateway"}
```

Producto:

```logql
{service="producto"}
```

Catalogo:

```logql
{service="catalogo"}
```

Como explicarlo:

- Prometheus muestra que hubo trafico o error
- Loki muestra el mensaje concreto registrado por la aplicacion
- si aparece el mismo `traceId`, se puede seguir una peticion entre servicios

Lectura de la consulta:

```text
{service="producto"}
```

se lee como:

```text
Loki, muestrame todos los logs del servicio producto.
```

Partes importantes:

- `{service="producto"}` selecciona solo logs que tengan la etiqueta `service` con valor `producto`
- esa etiqueta no viene del log original; la agrega Promtail
- si esta consulta devuelve resultados, significa que Loki y Promtail estan funcionando para ese servicio

---

### 2.3 Reducir ruido por texto

Cuando ya se confirmo que hay logs, recien se puede filtrar por texto.

Ejemplo general:

```logql
{service="producto"} |= "PRODUCTO"
```

Esto busca lineas del servicio `producto` que contengan la palabra `PRODUCTO`.

Si la aplicacion registra mensajes funcionales con corchetes, tambien se puede usar:

```logql
{service="producto"} |= "[PRODUCTO]"
```

Pero esta consulta solo funciona si el texto exacto `[PRODUCTO]` aparece en el log.

Si no devuelve resultados, no significa necesariamente que Loki este mal. Puede significar que ese texto exacto no existe en las lineas actuales.

Consultas utiles:

Gateway:

```logql
{service="gateway"} |= "GATEWAY"
```

Producto:

```logql
{service="producto"} |= "PRODUCTO"
```

Catalogo:

```logql
{service="catalogo"} |= "CATALOGO"
```

Version mas estricta, solo si existen esos textos exactos:

```logql
{service="gateway"} |= "[GATEWAY]"
```

```logql
{service="producto"} |= "[PRODUCTO]"
```

```logql
{service="catalogo"} |= "[CATALOGO]"
```

Lectura de esta consulta:

```text
{service="producto"} |= "[PRODUCTO]"
```

se lee como:

```text
Loki, muestrame solo logs del servicio producto
y dentro de esos logs quedate solo con las lineas que contienen [PRODUCTO].
```

Partes importantes:

- `{service="producto"}` selecciona solo logs de producto
- `|=` filtra por texto dentro del mensaje
- `"[PRODUCTO]"` es el texto que debe aparecer en la linea del log

Esto ayuda a quitar ruido de framework y quedarse con logs funcionales de la aplicacion.

Regla practica para principiantes:

1. Primero probar `{service="producto"}`.
2. Si aparecen logs, Loki funciona.
3. Luego agregar filtros como `|= "PRODUCTO"`.
4. Si el filtro no devuelve nada, quitar el filtro y buscar que texto aparece realmente en los logs.

### 2.4 Que hacer si no aparecen logs

Revisar en este orden:

1. Confirmar que el datasource sea `Loki`, no `Prometheus`.
2. Usar `Explore` normal, no `Drilldown -> Logs`.
3. Cambiar el rango a `Last 15 minutes` o `Last 1 hour`.
4. Generar trafico otra vez:

```text
http://localhost:8090/api/v1/productos/detalle/1
```

5. Probar una consulta sin filtro de texto:

```logql
{service="producto"}
```

6. Si aparece `App not found`, volver a:

```text
http://localhost:13000/explore
```

Mensaje clave:

```text
Los logs explican la causa probable de lo que las metricas detectan.
```

---

## Caso 3. Falla controlada

Este caso une metricas y logs.

### 3.1 Provocar la falla

Detener `catalogo`.

Luego ejecutar:

```text
http://localhost:8090/api/v1/productos/detalle/1
```

Como explicar la prueba:

```text
Detener catalogo
```

simula que un microservicio necesario dejo de estar disponible.

En esta practica DEV, `catalogo` normalmente esta corriendo con Maven. Para detenerlo, usar `Ctrl + C` en la terminal donde se ejecuto `mvn spring-boot:run`.

Nota:

```text
No detener mysql-catalogo-dev.
La idea es detener la aplicacion catalogo, no su base de datos.
```

Si se estuviera trabajando en PROD con Docker, ahi si se detendria el contenedor de la aplicacion `catalogo`.

Despues se vuelve a ejecutar:

```text
http://localhost:8090/api/v1/productos/detalle/1
```

porque ese flujo permite observar si `producto` falla, responde con fallback o registra un error al intentar comunicarse con `catalogo`.

### 3.2 Mirar metricas

Disponibilidad:

```promql
up
```

Errores:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

Como leer estas dos metricas juntas:

```text
up
```

responde:

```text
El servicio sigue disponible para Prometheus?
```

La consulta de errores responde:

```text
Algun servicio esta devolviendo errores 5xx?
```

Si `catalogo-dev` aparece en `0`, el problema esta en disponibilidad.

Si `producto-dev` o `gateway-dev` muestran `5xx`, el problema ya esta impactando una peticion HTTP.

Que se espera:

- `catalogo-dev` puede pasar a `0`
- pueden aparecer errores en `producto-dev` o `gateway-dev`
- las metricas indican que algo fallo

### 3.3 Mirar logs

Consulta recomendada:

```logql
{service="producto"}
```

Si hay mucho ruido, filtrar por una palabra que si aparezca en el log:

```logql
{service="producto"} |= "PRODUCTO"
```

Tambien se puede revisar:

```logql
{service=~"gateway|producto|catalogo"}
```

Que se espera:

- error al intentar llamar a `catalogo`
- mensaje de excepcion, timeout o fallback
- posible `traceId` para relacionar la peticion

Conclusion del caso:

```text
Metricas: detectan el sintoma.
Logs: ayudan a explicar la causa.
```

---

## Caso 4. Alerta simple en Grafana

Este caso responde:

```text
Como me entero si un servicio se cae?
```

La alerta mas simple para la clase sera:

```text
catalogo-dev esta caido
```

### 4.1 Crear regla de alerta

En Grafana:

1. Ir al menu izquierdo.
2. Entrar a `Alerting`.
3. Entrar a `Alert rules`.
4. Presionar `New alert rule`.

Si el menu lateral esta colapsado, buscar el icono de alerta o escribir en la barra superior:

```text
Alert rules
```

Luego completar la regla.

Nombre de la regla:

```text
Catalogo caido
```

Datasource:

```text
Prometheus
```

Consulta:

```promql
up{job="catalogo-dev"}
```

Lectura de la consulta:

```text
up{job="catalogo-dev"}
```

se lee como:

```text
Prometheus, dime solo si catalogo-dev esta disponible.
```

Partes importantes:

- `up` consulta disponibilidad
- `{job="catalogo-dev"}` filtra solo el servicio catalogo
- si devuelve `1`, catalogo esta disponible para Prometheus
- si devuelve `0`, catalogo no esta disponible para Prometheus

Condicion:

```text
IS BELOW 1
```

Como leer la condicion:

```text
IS BELOW 1
```

significa:

```text
dispara la alerta si el valor esta por debajo de 1
```

Como `up` normalmente vale `1` o `0`, esta condicion equivale a:

```text
si catalogo-dev vale 0, activar alerta
```

En algunas versiones de Grafana, la condicion se arma con bloques parecidos a estos:

```text
A = consulta Prometheus
B = Reduce / Last de A
C = Threshold: B is below 1
```

Como explicarlo:

- `A` trae el valor de Prometheus
- `B` toma el ultimo valor recibido
- `C` compara ese valor contra `1`
- si el ultimo valor es `0`, la alerta entra en condicion de falla

Evaluacion sugerida:

```text
Every 30s for 1m
```

Como leer la evaluacion:

```text
Every 30s
```

significa que Grafana revisa la regla cada 30 segundos.

```text
for 1m
```

significa que la condicion debe mantenerse fallando durante 1 minuto antes de activar la alerta.

Ejemplo:

```text
Si catalogo cae por 10 segundos y vuelve, no alerta.
Si catalogo sigue caido por 1 minuto, la alerta se activa.
```

Folder:

```text
Observabilidad
```

Si no existe ese folder, se puede usar:

```text
General
```

Evaluation group:

```text
microservicios
```

Si Grafana pide crear uno, usar:

```text
Evaluate every: 30s
```

Mensaje o annotation:

```text
Prometheus no puede consultar catalogo-dev. Revisar si el servicio esta levantado y si el puerto 8081 responde.
```

Guardar con:

```text
Save rule and exit
```

Para esta practica no es obligatorio configurar envio por correo, Slack o Teams. Lo importante es que la alerta exista y cambie de estado dentro de Grafana.

### 4.2 Como probarla

1. Asegurar que `catalogo` esta levantado.
2. Ver que la alerta esta normal.
3. Detener `catalogo`.
4. Esperar la evaluacion.
5. Confirmar que la alerta cambia de estado.

### Como explicarlo

Una alerta no reemplaza al dashboard ni a los logs.

La alerta solo avisa:

```text
algo requiere atencion
```

Luego se investiga con:

- metricas para ver el impacto
- logs para ver la causa

---

## Dashboard minimo recomendado

Para una clase de 30 minutos, crear solo estos paneles:

| Panel | Consulta | Para que sirve |
|---|---|---|
| Servicios vivos | `up` | saber si Prometheus consulta los servicios |
| Requests por segundo | `sum by (job) (rate(http_server_requests_seconds_count[1m]))` | ver trafico |
| Errores 5xx | `sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))` | detectar fallas HTTP |
| Logs de servicios | `{service=~"gateway|producto|catalogo"}` | explicar lo ocurrido |

Orden de lectura:

1. Esta vivo?
2. Tiene trafico?
3. Tiene errores?
4. Que dicen los logs?
5. Hay una alerta activa?

---

## Checklist de cierre

El alumno debe mostrar:

- `targets` en Prometheus con servicios `UP`
- dashboard con `up`
- dashboard con requests por segundo
- dashboard con errores 5xx
- consulta Loki por `service`
- evidencia de una peticion `gateway -> producto -> catalogo`
- una falla controlada deteniendo `catalogo`
- alerta `Catalogo caido`

---

## Cierre conceptual

Frase para cerrar la clase:

```text
Las metricas me dicen que algo paso.
Los logs me ayudan a entender por que paso.
Las alertas me avisan cuando debo mirar.
```
