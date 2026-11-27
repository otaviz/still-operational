param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$jar = Join-Path $root 'target\still-operational-1.0-SNAPSHOT.jar'
$icon = Join-Path $root 'StillOperational.ico'
$dest = Join-Path $root 'dist'
$app = Join-Path $dest 'StillOperational'

if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) {
    throw 'JAR nao encontrado. No IntelliJ, execute Maven > Lifecycle > clean e depois package.'
}
if (-not (Test-Path -LiteralPath $icon -PathType Leaf)) {
    throw 'Coloque StillOperational.ico na raiz do projeto, ao lado de pom.xml.'
}
if (Test-Path -LiteralPath $app) {
    throw 'Ja existe dist\StillOperational. Renomeie essa pasta antes de gerar outra versao.'
}
if ($JdkHome) {
    $jpackage = Join-Path $JdkHome 'bin\jpackage.exe'
    if (-not (Test-Path -LiteralPath $jpackage -PathType Leaf)) {
        throw 'jpackage.exe nao encontrado no JDK indicado. Use -JdkHome com o caminho do JDK 25.'
    }
} else {
    $cmd = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if (-not $cmd) { throw 'JDK nao localizado. Execute o script com -JdkHome e o caminho do JDK 25 usado pelo IntelliJ.' }
    $jpackage = $cmd.Source
}
$version = & $jpackage --version
if ($LASTEXITCODE -ne 0 -or "$version" -notmatch '^(\d+)') { throw 'Nao foi possivel verificar a versao do jpackage.' }
if ([int]$Matches[1] -lt 25) { throw 'Use um JDK 25 ou superior: o projeto compila para Java 25.' }

# Confirma que a classe grafica e os recursos estao dentro do JAR.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
try {
    foreach ($entry in @('com/stilloperational/JanelaPrincipal.class','com/stilloperational/PainelFlames.class','flames.png','logo.png','audio/musica_fundo.wav')) {
        if ($null -eq $zip.GetEntry($entry)) { throw "JAR incompleto: $entry. Execute clean e package novamente." }
    }
} finally { $zip.Dispose() }

$inputDir = Join-Path ([System.IO.Path]::GetTempPath()) ('still-operational-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $inputDir | Out-Null
try {
    Copy-Item -LiteralPath $jar -Destination $inputDir
    New-Item -ItemType Directory -Path $dest -Force | Out-Null
    $jpackageArgs = @('--type','app-image','--name','StillOperational','--app-version','1.0.0',
        '--input',$inputDir,'--main-jar',(Split-Path -Leaf $jar),
        '--main-class','com.stilloperational.JanelaPrincipal','--icon',$icon,
        '--dest',$dest,'--add-modules','java.desktop')
    & $jpackage @jpackageArgs
    if ($LASTEXITCODE -ne 0) { throw "jpackage falhou (codigo $LASTEXITCODE)." }
    $exe = Join-Path $app 'StillOperational.exe'
    if (-not (Test-Path -LiteralPath $exe -PathType Leaf)) { throw 'Executavel nao localizado na pasta esperada.' }
    Write-Host "Executavel criado: $exe" -ForegroundColor Green
    Write-Host 'Para distribuir, preserve a pasta StillOperational inteira.'
} finally {
    Remove-Item -LiteralPath $inputDir -Recurse -Force
}
