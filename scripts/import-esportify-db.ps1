param(
    [string]$SqlFile = ".\\database\\esportify-2.sql",
    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbName = "esportify",
    [string]$DbUser = "root",
    [string]$DbPassword = "",
    [bool]$Recreate = $true
)

$ErrorActionPreference = "Stop"

function Resolve-MySqlPath {
    $command = Get-Command mysql -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        "C:\\xampp\\mysql\\bin\\mysql.exe",
        "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
        "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",
        "C:\\Program Files\\MariaDB 10.4\\bin\\mysql.exe",
        "C:\\Program Files\\MariaDB 10.11\\bin\\mysql.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "mysql.exe introuvable. Installe MySQL/MariaDB ou ajoute mysql au PATH."
}

if (-not (Test-Path -LiteralPath $SqlFile)) {
    throw "Fichier SQL introuvable: $SqlFile"
}

$mysqlPath = Resolve-MySqlPath
$authArgs = @("-h", $DbHost, "-P", "$DbPort", "-u", $DbUser)
if ($DbPassword -ne "") {
    $authArgs += "--password=$DbPassword"
}

if ($Recreate) {
    $dbSql = "DROP DATABASE IF EXISTS $DbName; CREATE DATABASE $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
} else {
    $dbSql = "CREATE DATABASE IF NOT EXISTS $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}
& $mysqlPath @authArgs -e $dbSql
if ($LASTEXITCODE -ne 0) {
    throw "Echec creation/recreation de la base $DbName."
}

$importArgs = @() + $authArgs + @($DbName)
Get-Content -LiteralPath $SqlFile -Raw | & $mysqlPath @importArgs
if ($LASTEXITCODE -ne 0) {
    throw "Echec import SQL dans la base $DbName."
}

Write-Host "Import termine. Base active: $DbName"
