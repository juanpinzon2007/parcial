[CmdletBinding()]
param(
    [switch]$BuildImages,
    [switch]$SkipFrontend,
    [switch]$SkipBackend
)

$ErrorActionPreference = "Stop"

function Require-Env {
    param([string]$Name)
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Falta la variable de entorno requerida: $Name"
    }
    return $value
}

function Optional-Env {
    param(
        [string]$Name,
        [string]$Default = ""
    )
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $Default
    }
    return $value
}

function Quote-Yaml {
    param([string]$Value)
    return "'" + $Value.Replace("'", "''") + "'"
}

function New-EnvFile {
    param(
        [string]$Name,
        [hashtable]$Values
    )

    $tempDir = Join-Path ([System.IO.Path]::GetTempPath()) "papeleria-cloud-run"
    New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

    $path = Join-Path $tempDir $Name
    $lines = foreach ($key in $Values.Keys) {
        "$key`: $(Quote-Yaml $Values[$key])"
    }
    Set-Content -Path $path -Value $lines -Encoding utf8
    return $path
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent (Split-Path -Parent $scriptDir)

$projectId = Require-Env "PROJECT_ID"
$region = Optional-Env "REGION" "us-central1"
$repository = Optional-Env "REPOSITORY" "papeleria"
$imageTag = Optional-Env "IMAGE_TAG" "latest"

$mysqlInstance = Require-Env "MYSQL_INSTANCE"
$postgresInstance = Require-Env "POSTGRES_INSTANCE"
$authDbName = Optional-Env "AUTH_DB_NAME" "auth_db"
$inventoryDbName = Optional-Env "INVENTORY_DB_NAME" "inventory_db"
$salesDbName = Optional-Env "SALES_DB_NAME" "sales_db"

$mysqlUser = Require-Env "MYSQL_USER"
$mysqlPassword = Require-Env "MYSQL_PASSWORD"
$postgresUser = Require-Env "POSTGRES_USER"
$postgresPassword = Require-Env "POSTGRES_PASSWORD"
$jwtSecret = Require-Env "JWT_SECRET"

$adminEmail = Optional-Env "ADMIN_EMAIL" "admin@papeleria.com"
$adminPassword = Optional-Env "ADMIN_PASSWORD" "Admin123*"
$adminFullName = Optional-Env "ADMIN_FULL_NAME" "Administrador General"

$authImage = "$region-docker.pkg.dev/$projectId/$repository/auth-service:$imageTag"
$inventoryImage = "$region-docker.pkg.dev/$projectId/$repository/inventory-service:$imageTag"
$salesImage = "$region-docker.pkg.dev/$projectId/$repository/sales-service:$imageTag"
$frontendImage = "$region-docker.pkg.dev/$projectId/$repository/frontend:$imageTag"

$mysqlConnectionName = "$projectId`:$region`:$mysqlInstance"
$postgresConnectionName = "$projectId`:$region`:$postgresInstance"
$tempFiles = @()

try {
    if ($BuildImages) {
        Write-Host "Construyendo y publicando imagenes en Artifact Registry..."
        & gcloud builds submit $repoRoot `
            --config "$scriptDir\cloudbuild.yaml" `
            --project $projectId `
            --substitutions "_REGION=$region,_REPOSITORY=$repository,_TAG=$imageTag"
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo la construccion de imagenes en Cloud Build."
        }
    }

    if (-not $SkipBackend) {
        $frontendUrl = Require-Env "FRONTEND_URL"
        $authEnvFile = New-EnvFile "auth-service.env.yaml" @{
            DB_URL = "jdbc:mysql:///$authDbName?cloudSqlInstance=$mysqlConnectionName&socketFactory=com.google.cloud.sql.mysql.SocketFactory&ipTypes=PUBLIC,PRIVATE&cloudSqlRefreshStrategy=lazy"
            DB_USERNAME = $mysqlUser
            DB_PASSWORD = $mysqlPassword
            APP_JWT_SECRET = $jwtSecret
            CORS_ALLOWED_ORIGIN = $frontendUrl
            ADMIN_EMAIL = $adminEmail
            ADMIN_PASSWORD = $adminPassword
            ADMIN_FULL_NAME = $adminFullName
        }
        $tempFiles += $authEnvFile
        $inventoryEnvFile = New-EnvFile "inventory-service.env.yaml" @{
            DB_URL = "jdbc:postgresql:///$inventoryDbName?cloudSqlInstance=$postgresConnectionName&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC,PRIVATE&cloudSqlRefreshStrategy=lazy"
            DB_USERNAME = $postgresUser
            DB_PASSWORD = $postgresPassword
            APP_JWT_SECRET = $jwtSecret
            CORS_ALLOWED_ORIGIN = $frontendUrl
        }
        $tempFiles += $inventoryEnvFile
        $salesEnvFile = New-EnvFile "sales-service.env.yaml" @{
            DB_URL = "jdbc:mysql:///$salesDbName?cloudSqlInstance=$mysqlConnectionName&socketFactory=com.google.cloud.sql.mysql.SocketFactory&ipTypes=PUBLIC,PRIVATE&cloudSqlRefreshStrategy=lazy"
            DB_USERNAME = $mysqlUser
            DB_PASSWORD = $mysqlPassword
            APP_JWT_SECRET = $jwtSecret
            CORS_ALLOWED_ORIGIN = $frontendUrl
        }
        $tempFiles += $salesEnvFile

        Write-Host "Desplegando auth-service..."
        & gcloud run deploy auth-service `
            --project $projectId `
            --region $region `
            --platform managed `
            --image $authImage `
            --allow-unauthenticated `
            --add-cloudsql-instances $mysqlConnectionName `
            --env-vars-file $authEnvFile
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo el despliegue de auth-service."
        }

        Write-Host "Desplegando inventory-service..."
        & gcloud run deploy inventory-service `
            --project $projectId `
            --region $region `
            --platform managed `
            --image $inventoryImage `
            --allow-unauthenticated `
            --add-cloudsql-instances $postgresConnectionName `
            --env-vars-file $inventoryEnvFile
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo el despliegue de inventory-service."
        }

        Write-Host "Desplegando sales-service..."
        & gcloud run deploy sales-service `
            --project $projectId `
            --region $region `
            --platform managed `
            --image $salesImage `
            --allow-unauthenticated `
            --add-cloudsql-instances $mysqlConnectionName `
            --env-vars-file $salesEnvFile
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo el despliegue de sales-service."
        }
    }

    if (-not $SkipFrontend) {
        $authPublicUrl = Require-Env "AUTH_PUBLIC_URL"
        $inventoryPublicUrl = Require-Env "INVENTORY_PUBLIC_URL"
        $salesPublicUrl = Require-Env "SALES_PUBLIC_URL"
        $frontendEnvFile = New-EnvFile "frontend.env.yaml" @{
            AUTH_API_URL = "$authPublicUrl/api"
            INVENTORY_API_URL = "$inventoryPublicUrl/api"
            SALES_API_URL = "$salesPublicUrl/api"
        }
        $tempFiles += $frontendEnvFile

        Write-Host "Desplegando papeleria-frontend..."
        & gcloud run deploy papeleria-frontend `
            --project $projectId `
            --region $region `
            --platform managed `
            --image $frontendImage `
            --allow-unauthenticated `
            --env-vars-file $frontendEnvFile
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo el despliegue del frontend."
        }
    }

    Write-Host "Despliegue finalizado."
}
finally {
    if ($tempFiles.Count -gt 0) {
        $tempFiles | ForEach-Object {
            if (Test-Path $_) {
                Remove-Item -LiteralPath $_ -Force
            }
        }
    }
}
