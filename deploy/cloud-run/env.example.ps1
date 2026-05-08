$env:PROJECT_ID = "tu-proyecto"
$env:REGION = "us-central1"
$env:REPOSITORY = "papeleria"
$env:IMAGE_TAG = "latest"

$env:MYSQL_INSTANCE = "papeleria-mysql"
$env:POSTGRES_INSTANCE = "papeleria-postgres"
$env:AUTH_DB_NAME = "auth_db"
$env:INVENTORY_DB_NAME = "inventory_db"
$env:SALES_DB_NAME = "sales_db"

$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "cambia-esta-clave"
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "cambia-esta-clave"
$env:JWT_SECRET = "reemplaza-por-un-secreto-largo-y-unico"

$env:ADMIN_EMAIL = "admin@papeleria.com"
$env:ADMIN_PASSWORD = "Admin123*"
$env:ADMIN_FULL_NAME = "Administrador General"

# Usa la URL final del frontend cuando actualices CORS en backend.
$env:FRONTEND_URL = "https://papeleria-frontend-xxxxxxxxxx-uc.a.run.app"
