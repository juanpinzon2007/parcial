# Despliegue en Google Cloud Run

Configuracion actualizada para desplegar las cuatro imagenes en Cloud Run y conectar los backends a Cloud SQL.

Archivos incluidos:

- `cloudbuild.yaml`: construye y publica `auth-service`, `inventory-service`, `sales-service` y `frontend` en Artifact Registry.
- `deploy.ps1`: automatiza build y despliegue desde PowerShell.
- `env.example.ps1`: plantilla de variables de entorno.

## 1. APIs y prerequisitos

Habilita estas APIs:

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  sqladmin.googleapis.com
```

Crea un repositorio Docker en Artifact Registry:

```bash
gcloud artifacts repositories create papeleria \
  --repository-format=docker \
  --location=us-central1
```

## 2. Bases de datos

Se esperan dos instancias de Cloud SQL:

1. MySQL 8
   - instancia: `papeleria-mysql`
   - bases: `auth_db`, `sales_db`
2. PostgreSQL 16
   - instancia: `papeleria-postgres`
   - base: `inventory_db`

En MySQL debes crear:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE sales_db;
```

## 3. Conexion desde Cloud Run

Los microservicios Java ya incluyen el Cloud SQL Java Connector en sus `pom.xml`, por eso usan URLs JDBC de este estilo:

- MySQL:
  `jdbc:mysql:///<DB_NAME>?cloudSqlInstance=<PROJECT_ID>:<REGION>:<INSTANCE_NAME>&socketFactory=com.google.cloud.sql.mysql.SocketFactory&ipTypes=PUBLIC,PRIVATE&cloudSqlRefreshStrategy=lazy`
- PostgreSQL:
  `jdbc:postgresql:///<DB_NAME>?cloudSqlInstance=<PROJECT_ID>:<REGION>:<INSTANCE_NAME>&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC,PRIVATE&cloudSqlRefreshStrategy=lazy`

La documentacion oficial de Google Cloud sigue mostrando `--add-cloudsql-instances` para despliegues por CLI, y el script lo conserva por consistencia operativa.

## 4. Variables de entorno

Carga la plantilla y ajusta valores reales:

```powershell
. .\deploy\cloud-run\env.example.ps1
```

Variables obligatorias:

- `PROJECT_ID`
- `MYSQL_INSTANCE`
- `POSTGRES_INSTANCE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `JWT_SECRET`

Variables usadas en el despliegue:

- `FRONTEND_URL`: URL publica del frontend para CORS en backend.
- `AUTH_PUBLIC_URL`
- `INVENTORY_PUBLIC_URL`
- `SALES_PUBLIC_URL`

## 5. Construir imagenes

Para publicar las cuatro imagenes con Cloud Build desde la raiz del repositorio:

```powershell
gcloud builds submit . `
  --config .\deploy\cloud-run\cloudbuild.yaml `
  --substitutions _REGION=us-central1,_REPOSITORY=papeleria,_TAG=latest
```

Ese `cloudbuild.yaml` hace dos cosas para backend:

- compila cada microservicio Java con Maven (`clean package -DskipTests`)
- construye una imagen runtime usando `Dockerfile.runtime`

Rutas usadas:

- `auth-service/pom.xml` y `auth-service/Dockerfile.runtime`
- `inventory-service/pom.xml` y `inventory-service/Dockerfile.runtime`
- `sales-service/pom.xml` y `sales-service/Dockerfile.runtime`
- `frontend/Dockerfile` con contexto `frontend`

O usando el script:

```powershell
.\deploy\cloud-run\deploy.ps1 -BuildImages -SkipBackend -SkipFrontend
```

## 6. Desplegar backend

Cuando ya tengas la URL publica del frontend, define `FRONTEND_URL` y despliega:

```powershell
.\deploy\cloud-run\deploy.ps1 -SkipFrontend
```

El script despliega:

- `auth-service`
- `inventory-service`
- `sales-service`

Todos quedan configurados con:

- `PORT` dinamico de Cloud Run por variable de entorno
- `DB_URL` con Cloud SQL Java Connector
- `APP_JWT_SECRET` comun
- `CORS_ALLOWED_ORIGIN` apuntando al frontend publico

## 7. Desplegar frontend

Una vez tengas las URLs publicas de los tres backends, define:

```powershell
$env:AUTH_PUBLIC_URL = "https://auth-service-xxxxx-uc.a.run.app"
$env:INVENTORY_PUBLIC_URL = "https://inventory-service-xxxxx-uc.a.run.app"
$env:SALES_PUBLIC_URL = "https://sales-service-xxxxx-uc.a.run.app"
```

Luego despliega:

```powershell
.\deploy\cloud-run\deploy.ps1 -SkipBackend
```

El frontend publica `runtime-config.js` en arranque y toma:

- `AUTH_API_URL`
- `INVENTORY_API_URL`
- `SALES_API_URL`

## 8. Orden recomendado

1. Construye y publica imagenes.
2. Despliega el frontend temporalmente o define su URL final si ya existe.
3. Despliega backend con `FRONTEND_URL`.
4. Recupera las URLs publicas de `auth-service`, `inventory-service` y `sales-service`.
5. Despliega o actualiza `papeleria-frontend` con esas URLs.
6. Si cambias la URL del frontend, vuelve a desplegar backend para refrescar CORS.

## 9. Notas operativas

- El archivo raiz `.gcloudignore` evita subir `node_modules`, `dist`, `target` y directorios locales innecesarios a Cloud Build.
- `frontend` ya soporta `PORT` dinamico, requisito de Cloud Run.
- Guarda credenciales y `JWT_SECRET` en Secret Manager en un entorno real.

## Referencias oficiales

- Cloud SQL MySQL desde Cloud Run: https://cloud.google.com/sql/docs/mysql/connect-run
- Cloud SQL PostgreSQL desde Cloud Run: https://cloud.google.com/sql/docs/postgres/connect-run
- YAML de Cloud Run: https://cloud.google.com/run/docs/reference/yaml/v1
