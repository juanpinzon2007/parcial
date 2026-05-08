# Papeleria Suite

Proyecto de parcial con microservicios para una papeleria. Incluye:

- `auth-service`: registro, login, JWT y roles `ADMIN` / `USER`.
- `inventory-service`: CRUD de productos en PostgreSQL.
- `sales-service`: CRUD de pedidos en MySQL.
- `frontend`: Angular con consumo HTTP, vistas por rol y formularios CRUD.

## Arquitectura

- Backend con arquitectura en capas: `controller`, `service`, `repository`, `model`, `dto`.
- Persistencia distribuida:
  - MySQL: `auth_db` y `sales_db`
  - PostgreSQL: `inventory_db`
- Despliegue local con `docker-compose`.
- Preparado para Cloud Run + Cloud SQL en Google Cloud.

## Credenciales iniciales

- Admin:
  - correo: `admin@papeleria.com`
  - clave: `Admin123*`

Los usuarios creados desde registro se guardan con rol `USER`.

## Ejecucion local

Requisitos:

- Docker Desktop

Comando:

```bash
docker compose up --build
```

Servicios:

- Frontend: `http://localhost:4200`
- Auth API: `http://localhost:8081/api`
- Inventory API: `http://localhost:8082/api`
- Sales API: `http://localhost:8083/api`

## Despliegue en Google Cloud

La guia detallada esta en [deploy/cloud-run/README.md](deploy/cloud-run/README.md).
