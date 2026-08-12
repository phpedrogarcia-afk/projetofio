[CmdletBinding()]
param(
    [string[]]$AvdNames = @("Fio_API_36_Primary", "Fio_API_26_Oldest"),
    [switch]$SkipBuild,
    [switch]$KeepEmulators
)

$ErrorActionPreference = "Stop"
$syntheticPin = "2468"
$packageName = "com.projetofio.app"
$activity = "$packageName/.MainActivity"
$receiver = "$packageName/com.projetofio.app.testing.DebugFixtureReceiver"

$sdkCandidates = @(
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    (Join-Path $env:LOCALAPPDATA "Android\Sdk")
) | Where-Object { $_ -and (Test-Path -LiteralPath $_) }
$androidSdk = $sdkCandidates | Select-Object -First 1
if (-not $androidSdk) { throw "Android SDK not found." }

$adb = Join-Path $androidSdk "platform-tools\adb.exe"
$emulator = Join-Path $androidSdk "emulator\emulator.exe"
$gradleRoot = Split-Path -Parent $PSScriptRoot
$gradle = Join-Path $gradleRoot "gradlew.bat"
$debugApk = Join-Path $gradleRoot "mobile\build\outputs\apk\debug\mobile-debug.apk"
$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk

function Invoke-Checked {
    param([string]$FilePath, [string[]]$Arguments)
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

function Get-EmulatorSerials {
    @(& $adb devices | Select-String '^emulator-\d+\s+device$' | ForEach-Object {
        (($_.Line -split '\s+')[0]).Trim()
    })
}

function Get-AvdName([string]$Serial) {
    @(& $adb -s $Serial emu avd name 2>$null | Where-Object { $_ -and $_ -ne "OK" }) |
        Select-Object -First 1
}

function Find-AvdSerial([string]$AvdName) {
    foreach ($serial in Get-EmulatorSerials) {
        if ((Get-AvdName $serial) -eq $AvdName) { return $serial }
    }
    return $null
}

function Wait-ForAvd([string]$AvdName, [int]$TimeoutSeconds = 180) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $serial = Find-AvdSerial $AvdName
        if ($serial) {
            $booted = (& $adb -s $serial shell getprop sys.boot_completed 2>$null).Trim()
            if ($booted -eq "1") { return $serial }
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "Timed out waiting for AVD '$AvdName'."
}

function Get-UiXml([string]$Serial) {
    $raw = (& $adb -s $Serial exec-out uiautomator dump /dev/tty 2>$null) -join ""
    $start = $raw.IndexOf("<?xml")
    $end = $raw.LastIndexOf("</hierarchy>")
    if ($start -lt 0 -or $end -lt 0) { throw "Could not read UI hierarchy from $Serial." }
    [xml]$raw.Substring($start, $end + "</hierarchy>".Length - $start)
}

function Wait-ForUiText([string]$Serial, [string]$Expected, [int]$TimeoutSeconds = 20) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $xml = Get-UiXml $Serial
        if ($xml.SelectSingleNode("//*[@text='$Expected']")) { return $xml }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)
    $visibleText = @($xml.SelectNodes("//*[@text!='']") | ForEach-Object { $_.text } | Sort-Object -Unique)
    throw "Text '$Expected' did not appear on $Serial. Visible text: $($visibleText -join ' | ')"
}

function Tap-UiText([string]$Serial, [string]$Text) {
    $xml = Wait-ForUiText $Serial $Text
    $node = $xml.SelectSingleNode("//*[@text='$Text']")
    if ($node.bounds -notmatch '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') {
        throw "Could not parse bounds for '$Text'."
    }
    $x = ([int]$Matches[1] + [int]$Matches[3]) / 2
    $y = ([int]$Matches[2] + [int]$Matches[4]) / 2
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "tap", "$x", "$y")
}

function Cancel-SystemAuthentication([string]$Serial) {
    $api = [int](& $adb -s $Serial shell getprop ro.build.version.sdk).Trim()
    if ($api -gt 28) {
        Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "4")
        return
    }
    $xml = Get-UiXml $Serial
    $node = $xml.SelectSingleNode("//*[@text='CANCEL']")
    while ($node -and $node.clickable -ne "true" -and $node.ParentNode -is [System.Xml.XmlElement]) {
        $node = $node.ParentNode
    }
    if ($node -and $node.focused -eq "true") {
        Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "66")
        return
    }
    if ($node -and $node.bounds -match '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') {
        $x = [int](([int]$Matches[1] + [int]$Matches[3]) / 2)
        $y = [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
        Invoke-Checked $adb @("-s", $Serial, "shell", "input", "tap", "$x", "$y")
    } else {
        Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "4")
    }
}

function Unlock-Device([string]$Serial) {
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "224")
    Start-Sleep -Seconds 1
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "swipe", "540", "2200", "540", "600", "500")
    Start-Sleep -Seconds 1
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "text", $syntheticPin)
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "66")
    Start-Sleep -Seconds 2
    Invoke-Checked $adb @("-s", $Serial, "shell", "input", "keyevent", "3")
}

function Set-SyntheticPin([string]$Serial, [int]$TimeoutSeconds = 45) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $output = & $adb -s $Serial shell locksettings set-pin $syntheticPin 2>&1
        if ($LASTEXITCODE -eq 0) { return }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    throw "Android lock-settings service did not accept the synthetic PIN on $Serial. Last output: $($output -join ' | ')"
}

Push-Location $gradleRoot
try {
    if (-not $SkipBuild) {
        Invoke-Checked $gradle @("--no-daemon", ":mobile:assembleDebug")
    }
    if (-not (Test-Path -LiteralPath $debugApk)) { throw "Debug APK not found: $debugApk" }

    foreach ($avdName in $AvdNames) {
        $serial = Find-AvdSerial $avdName
        $startedHere = $false
        $pinWasSet = $false
        if (-not $serial) {
            Start-Process -FilePath $emulator -ArgumentList @(
                "-avd", $avdName,
                "-no-snapshot-load", "-no-snapshot-save", "-no-boot-anim",
                "-gpu", "auto"
            ) -WindowStyle Hidden | Out-Null
            $startedHere = $true
        }

        try {
            $serial = Wait-ForAvd $avdName
            Write-Host "== Credential profile: $avdName ($serial) =="
            Set-SyntheticPin $serial
            $pinWasSet = $true
            Unlock-Device $serial
            Invoke-Checked $adb @("-s", $serial, "install", "-r", $debugApk)
            Invoke-Checked $adb @(
                "-s", $serial, "shell", "am", "broadcast",
                "-a", "com.projetofio.app.testing.SET_APP_LOCK",
                "-n", $receiver,
                "--es", "mode", "IMMEDIATE"
            )
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "force-stop", $packageName)
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "start", "-W", "-n", $activity)

            Wait-ForUiText $serial "Abrir o Fio" | Out-Null
            $api = [int](& $adb -s $serial shell getprop ro.build.version.sdk).Trim()
            if ($api -gt 28) {
                Cancel-SystemAuthentication $serial
                Wait-ForUiText $serial "Protegido pelo bloqueio do seu aparelho." | Out-Null
                Tap-UiText $serial "Abrir"
                Wait-ForUiText $serial "Abrir o Fio" | Out-Null
                Invoke-Checked $adb @("-s", $serial, "shell", "input", "text", $syntheticPin)
                Invoke-Checked $adb @("-s", $serial, "shell", "input", "keyevent", "66")
                Wait-ForUiText $serial "Guardar" | Out-Null
                Write-Host "PASS: system credential prompt, cancellation, locked fallback, and PIN success"
            } else {
                Write-Host "PASS: legacy system credential prompt is present; interaction remains a manual platform gate"
            }
        } finally {
            if ($serial) {
                try { & $adb -s $serial shell am force-stop $packageName 2>$null } catch {}
                try { & $adb -s $serial shell pm clear $packageName 2>$null | Out-Null } catch {}
                if ($pinWasSet -and ((& $adb -s $serial get-state 2>$null) -eq "device")) {
                    try { & $adb -s $serial shell locksettings clear --old $syntheticPin 2>$null } catch {}
                }
            }
            if ($startedHere -and -not $KeepEmulators -and $serial) {
                & $adb -s $serial emu kill | Out-Null
            }
        }
    }
    Write-Host "M1 emulator credential profiles completed successfully."
} finally {
    Pop-Location
}
