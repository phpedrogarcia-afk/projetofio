[CmdletBinding()]
param(
    [string[]]$AvdNames = @("Fio_API_36_Primary", "Fio_API_26_Oldest"),
    [switch]$SkipBuild,
    [switch]$KeepEmulators
)

$ErrorActionPreference = "Stop"
$sdkCandidates = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA "Android\Sdk")) |
    Where-Object { $_ -and (Test-Path -LiteralPath $_) }
$androidSdk = $sdkCandidates | Select-Object -First 1
if (-not $androidSdk) { throw "Android SDK not found." }
$adb = Join-Path $androidSdk "platform-tools\adb.exe"
$emulator = Join-Path $androidSdk "emulator\emulator.exe"
$gradleRoot = Split-Path -Parent $PSScriptRoot
$gradle = Join-Path $gradleRoot "gradlew.bat"
$apk = Join-Path $gradleRoot "mobile\build\outputs\apk\validation\mobile-validation.apk"
$packageName = "com.projetofio.app.validation"
$activity = "$packageName/com.projetofio.app.MainActivity"
$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk

function Invoke-Checked([string]$FilePath, [string[]]$Arguments) {
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed: $FilePath $($Arguments -join ' ')" }
}

function Get-EmulatorSerials {
    @(& $adb devices | Select-String '^emulator-\d+\s+device$' | ForEach-Object { (($_.Line -split '\s+')[0]).Trim() })
}

function Get-AvdName([string]$Serial) {
    @(& $adb -s $Serial emu avd name 2>$null | Where-Object { $_ -and $_ -ne "OK" }) | Select-Object -First 1
}

function Find-AvdSerial([string]$AvdName) {
    foreach ($serial in Get-EmulatorSerials) { if ((Get-AvdName $serial) -eq $AvdName) { return $serial } }
    return $null
}

function Wait-ForAvd([string]$AvdName, [int]$TimeoutSeconds = 180) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $serial = Find-AvdSerial $AvdName
        if ($serial -and ((& $adb -s $serial shell getprop sys.boot_completed 2>$null).Trim() -eq "1")) { return $serial }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "Timed out waiting for $AvdName"
}

function Get-UiXml([string]$Serial) {
    $raw = (& $adb -s $Serial exec-out uiautomator dump /dev/tty 2>$null) -join ""
    $start = $raw.IndexOf("<?xml")
    $end = $raw.LastIndexOf("</hierarchy>")
    if ($start -lt 0 -or $end -lt 0) { throw "Could not read UI hierarchy." }
    [xml]$raw.Substring($start, $end + "</hierarchy>".Length - $start)
}

function Wait-ForUiText([string]$Serial, [string]$Expected, [int]$TimeoutSeconds = 30) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $xml = Get-UiXml $Serial
        $node = $xml.SelectSingleNode("//*[contains(@text,'$Expected')]")
        if ($node) { return $node }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)
    throw "Text '$Expected' did not appear on $Serial."
}

function Tap-UiText([string]$Serial, [string]$Text) {
    $node = Wait-ForUiText $Serial $Text
    while ($node -and $node.clickable -ne "true" -and $node.ParentNode -is [System.Xml.XmlElement]) { $node = $node.ParentNode }
    if ($node.bounds -notmatch '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') { throw "Could not parse '$Text' bounds." }
    $x = [int](([int]$Matches[1] + [int]$Matches[3]) / 2)
    $y = [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "tap", "$x", "$y")
}

function Scroll-ToUiText([string]$Serial, [string]$Text, [int]$Attempts = 8) {
    for ($index = 0; $index -lt $Attempts; $index++) {
        $xml = Get-UiXml $Serial
        $node = $xml.SelectSingleNode("//*[contains(@text,'$Text')]")
        if ($node) { return $node }
        Invoke-Checked $adb @("-s", $Serial, "shell", "input", "swipe", "950", "1500", "950", "350", "1000")
        Start-Sleep -Seconds 1
    }
    throw "Text '$Text' was not found after scrolling."
}

Push-Location $gradleRoot
try {
    if (-not $SkipBuild) { Invoke-Checked $gradle @("--no-daemon", ":mobile:assembleValidation") }
    if (-not (Test-Path -LiteralPath $apk)) { throw "Validation APK not found: $apk" }
    foreach ($avdName in $AvdNames) {
        $serial = Find-AvdSerial $avdName
        $startedHere = $false
        if (-not $serial) {
            Start-Process -FilePath $emulator -ArgumentList @("-avd", $avdName, "-no-snapshot-load", "-no-snapshot-save", "-no-boot-anim", "-gpu", "auto") -WindowStyle Hidden | Out-Null
            $startedHere = $true
        }
        try {
            $serial = Wait-ForAvd $avdName
            Write-Host "== M3 validation UI: $avdName ($serial) =="
            Invoke-Checked $adb @("-s", $serial, "install", "-r", $apk)
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "start", "-W", "-n", $activity)
            Wait-ForUiText $serial "O que est" | Out-Null
            Tap-UiText $serial "Configura"
            Scroll-ToUiText $serial "Importar" | Out-Null
            Scroll-ToUiText $serial "Escolher arquivo" | Out-Null
            Tap-UiText $serial "Escolher arquivo"
            Start-Sleep -Seconds 2
            $focus = (& $adb -s $serial shell dumpsys window windows) -join "`n"
            if ($focus -notmatch "documentsui|DocumentsActivity|picker") { throw "Android document picker did not receive the M3 request." }
            Write-Host "PASS: validation-only M3 surface and Android document picker"
        } finally {
            if ($serial) { & $adb -s $serial shell pm clear $packageName 2>$null | Out-Null }
            if ($startedHere -and -not $KeepEmulators -and $serial) { & $adb -s $serial emu kill | Out-Null }
        }
    }
    Write-Host "M3 validation UI profile completed successfully."
} finally {
    Pop-Location
}
