# Download Gradle dependencies that may fail (e.g. due to network timeout).
# Uses aria2 (multi-connection, resume) at C:\Users\swapnil\Downloads\aria2-1.37.0-win-64bit-build1,
# then curl, then Invoke-WebRequest.
# Run from project root: .\scripts\download-offline-deps.ps1
# Options: -ForceCurl  use curl/IWR instead of aria2

param(
    [switch]$ForceCurl
)

$ErrorActionPreference = "Stop"
$BaseUrl = "https://dl.google.com/dl/android/maven2"
$RepoRoot = Join-Path (Join-Path $PSScriptRoot "..") "local-maven-repo"
$Aria2Dir = "C:\Users\swapnil\Downloads\aria2-1.37.0-win-64bit-build1"
$Aria2Exe = Join-Path $Aria2Dir "aria2c.exe"

# Artifacts that have been failing: groupPath (Maven path), version, artifactId (base name)
$Artifacts = @(
    @{
        GroupPath  = "androidx/compose/material/material-icons-extended-android"
        Version    = "1.6.8"
        ArtifactId = "material-icons-extended-android"
    }
)

function Invoke-DownloadWithAria2 {
    param([string]$Url, [string]$OutFile)
    $dir = Split-Path $OutFile -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    # -x 16 max connections per server, -s 16 split, -k 1M min split size, -c continue/resume, -m 0 no timeout, --retry-wait=3
    & $Aria2Exe -x 16 -s 16 -k 1M -c -m 0 --retry-wait=3 --max-tries=5 -d $dir -o (Split-Path $OutFile -Leaf) $Url
    if ($LASTEXITCODE -ne 0) { throw "aria2 failed for $Url" }
}

function Invoke-DownloadWithCurl {
    param([string]$Url, [string]$OutFile)
    $dir = Split-Path $OutFile -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    & curl.exe -L -f -o $OutFile --retry 5 --retry-delay 3 --connect-timeout 30 --max-time 600 $Url
    if ($LASTEXITCODE -ne 0) { throw "curl failed for $Url" }
}

function Invoke-DownloadWithWebRequest {
    param([string]$Url, [string]$OutFile)
    $dir = Split-Path $OutFile -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $maxRetries = 5
    $attempt = 0
    while ($attempt -lt $maxRetries) {
        try {
            Invoke-WebRequest -Uri $Url -OutFile $OutFile -UseBasicParsing -TimeoutSec 600
            return
        } catch {
            $attempt++
            if ($attempt -eq $maxRetries) { throw }
            Start-Sleep -Seconds 3
        }
    }
}

$UseAria2 = -not $ForceCurl -and (Test-Path $Aria2Exe)

if ($UseAria2) {
    Write-Host "Using aria2 (external downloader): $Aria2Exe"
} else {
    if ($ForceCurl) { Write-Host "Using curl/Invoke-WebRequest ( -ForceCurl )." }
    else { Write-Host "aria2 not found; using curl/Invoke-WebRequest." }
}

foreach ($a in $Artifacts) {
    $groupPath = $a.GroupPath
    $version = $a.Version
    $artifactId = $a.ArtifactId
    $base = "$BaseUrl/$groupPath/$version/$artifactId-$version"
    $groupDir = Join-Path $RepoRoot ($groupPath -replace '/', [IO.Path]::DirectorySeparatorChar)
    $dir = Join-Path $groupDir $version
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

    foreach ($ext in @(".aar", ".pom")) {
        $url = "$base$ext"
        $outFile = Join-Path $dir "$artifactId-$version$ext"
        if (Test-Path $outFile) {
            Write-Host "Skip (exists): $artifactId-$version$ext"
            continue
        }

        Write-Host "Downloading: $url"
        try {
            if ($UseAria2) {
                Invoke-DownloadWithAria2 -Url $url -OutFile $outFile
            } elseif (Get-Command curl.exe -ErrorAction SilentlyContinue) {
                Invoke-DownloadWithCurl -Url $url -OutFile $outFile
            } else {
                Invoke-DownloadWithWebRequest -Url $url -OutFile $outFile
            }
            Write-Host "OK: $outFile"
        } catch {
            Write-Error "Failed to download $url : $_"
            exit 1
        }
    }
}

Write-Host "Done. Local Maven repo: $RepoRoot"
Write-Host "Run Gradle build; it will use these files when the local repo is configured."
