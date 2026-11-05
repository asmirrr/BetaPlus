param([string]$McpPath = (Join-Path $PSScriptRoot '..\mcp'))
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$reobf = Join-Path $McpPath 'minecraft\reobf'
$files = @('mod_BetaPlus.class', 'BetaPlusConfig.class', 'BetaPlusCoordinates.class', 'BetaPlusMap.class', 'BetaPlusWaypoints.class', 'BetaPlusWaypoints$Point.class', 'BetaPlusWaypointScreen.class')
foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $reobf $file) -PathType Leaf)) { throw "Missing reobfuscated class: $file. Recompile and reobfuscate first." }
}
$output = Join-Path $repo 'build'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$archive = Join-Path $output 'BetaPlus-dev.zip'
if (Test-Path -LiteralPath $archive) { throw "Output already exists: $archive. Move it aside before rebuilding." }
Compress-Archive -LiteralPath ($files | ForEach-Object { Join-Path $reobf $_ }) -DestinationPath $archive
Get-FileHash -LiteralPath $archive -Algorithm SHA256
