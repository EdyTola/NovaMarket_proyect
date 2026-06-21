# Arranque Keycloak DEV (NovaMarket)
# Uso: .\start-dev.ps1
#
# La primera vez compila la imagen (~5-15 min en Windows). Luego arranca en ~1 min.

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "==> Red Docker ecom-dev-net"
docker network create ecom-dev-net 2>$null

Write-Host "==> Compilar imagen Keycloak (solo la primera vez tarda varios minutos)..."
docker compose -f compose-dev.yml build keycloak
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "==> Levantar PostgreSQL + Keycloak (puerto 41880)..."
docker compose -f compose-dev.yml up -d
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "==> Esperando Keycloak..."
$maxAttempts = 36
$ready = $false

for ($i = 1; $i -le $maxAttempts; $i++) {
    $realm = curl.exe -s -m 8 -o NUL -w "%{http_code}" http://localhost:41880/realms/novamarket 2>$null
    $admin = curl.exe -s -m 8 -o NUL -w "%{http_code}" http://localhost:41880/admin/ 2>$null
    if ($realm -eq "200" -and ($admin -eq "200" -or $admin -eq "302" -or $admin -eq "303")) {
        $ready = $true
        break
    }

    $status = docker inspect -f "{{.State.Status}}" ecom-keycloak-dev 2>$null
    if ($status -ne "running") {
        Write-Host "Contenedor detenido. Logs:"
        docker logs --tail 40 ecom-keycloak-dev
        exit 1
    }

    Write-Host "  intento $i/$maxAttempts - realm:$realm admin:$admin"
    Start-Sleep -Seconds 10
}

if (-not $ready) {
    Write-Host "Keycloak aun no responde. Ultimos logs:"
    docker logs --tail 30 ecom-keycloak-dev
    exit 1
}

Write-Host ""
Write-Host "Keycloak listo."
Write-Host "  Admin:  http://localhost:41880/admin  (admin / admin)"
Write-Host "  Realm:  http://localhost:41880/realms/novamarket"
