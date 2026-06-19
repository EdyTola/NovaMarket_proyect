## Convencion de puertos

Base por microservicio:

- `db interna/base`: `90N0`
- `app interna`: `90N1`
- `db dev`: `19N0`
- `app dev`: `19N1`
- `db prod`: `29N0`
- `app prod`: `29N1`

Para `orden-ms` se usa `N=2`, por lo que queda:

- `db interna/base`: `9020`
- `app interna`: `9021`
- `db dev`: `19050`
- `app dev`: `19051`
- `db prod`: `29050`
- `app prod`: `29051`

## Convencion de proyecto

```text
Group Id:     com.upeu
Artifact Id:  orden-ms
Package Name: com.upeu.ordenms
```
