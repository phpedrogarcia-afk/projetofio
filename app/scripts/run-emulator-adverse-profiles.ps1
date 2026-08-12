[CmdletBinding()]
param(
    [string[]]$AvdNames = @("Fio_API_36_Primary", "Fio_API_26_Oldest"),
    [switch]$SkipBuild,
    [switch]$KeepEmulators
)

$ErrorActionPreference = "Stop"

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
$packageName = "com.projetofio.app"
$activity = "$packageName/.MainActivity"
$receiver = "$packageName/com.projetofio.app.testing.DebugFixtureReceiver"
$syntheticDraft = "FIO_SYNTHETIC_PROCESS_DEATH_DRAFT"

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

function Wait-ForUiText([string]$Serial, [string]$Expected, [int]$TimeoutSeconds = 30) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $xml = & $adb -s $Serial exec-out uiautomator dump /dev/tty 2>$null
        if (($xml -join "`n") -like "*$Expected*") { return }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "Text '$Expected' did not appear on $Serial."
}

function Assert-NoCrash([string]$Serial, [int]$SinceEpochSeconds) {
    $crashes = & $adb -s $Serial logcat -d -v epoch AndroidRuntime:E '*:S'
    $newCrash = @($crashes | Where-Object {
        if ($_ -match '^(\d+)\.') { [int64]$Matches[1] -ge $SinceEpochSeconds } else { $false }
    })
    if ($newCrash.Count -gt 0) {
        throw "AndroidRuntime crash detected after adverse-profile start: $($newCrash -join ' | ')"
    }
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
        if (-not $serial) {
            $startArguments = @{
                FilePath = $emulator
                ArgumentList = @(
                    "-avd", $avdName,
                    "-no-snapshot-load", "-no-snapshot-save", "-no-boot-anim",
                    "-gpu", "auto"
                )
                WindowStyle = "Hidden"
            }
            Start-Process @startArguments | Out-Null
            $startedHere = $true
        }

        try {
            $serial = Wait-ForAvd $avdName
            Write-Host "== Adverse profiles: $avdName ($serial) =="
            Invoke-Checked $adb @("-s", $serial, "install", "-r", $debugApk)
            & $adb -s $serial logcat -c

            Invoke-Checked $adb @(
                "-s", $serial, "shell", "am", "broadcast",
                "-a", "com.projetofio.app.testing.SEED_DRAFT",
                "-n", $receiver,
                "--es", "content", $syntheticDraft
            )
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "force-stop", $packageName)
            $startEpoch = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "start", "-W", "-n", $activity)
            Wait-ForUiText $serial $syntheticDraft
            Assert-NoCrash $serial $startEpoch

            & $adb -s $serial shell settings put system font_scale 1.5
            & $adb -s $serial shell settings put global window_animation_scale 0
            & $adb -s $serial shell settings put global transition_animation_scale 0
            & $adb -s $serial shell settings put global animator_duration_scale 0
            & $adb -s $serial shell settings put system accelerometer_rotation 0
            & $adb -s $serial shell settings put system user_rotation 1
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "force-stop", $packageName)
            $profileEpoch = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
            Invoke-Checked $adb @("-s", $serial, "shell", "am", "start", "-W", "-n", $activity)
            Wait-ForUiText $serial "Fio"
            Wait-ForUiText $serial $syntheticDraft
            Assert-NoCrash $serial $profileEpoch

            Write-Host "PASS: process death, font 1.5, landscape, animations off"
        } finally {
            if ($serial) {
                & $adb -s $serial shell settings put system font_scale 1.0 2>$null
                & $adb -s $serial shell settings put global window_animation_scale 1.0 2>$null
                & $adb -s $serial shell settings put global transition_animation_scale 1.0 2>$null
                & $adb -s $serial shell settings put global animator_duration_scale 1.0 2>$null
                & $adb -s $serial shell settings put system accelerometer_rotation 1 2>$null
                & $adb -s $serial shell settings put system user_rotation 0 2>$null
                & $adb -s $serial shell pm clear $packageName 2>$null | Out-Null
            }
            if ($startedHere -and -not $KeepEmulators -and $serial) {
                & $adb -s $serial emu kill | Out-Null
            }
        }
    }
    Write-Host "M1 adverse emulator profiles completed successfully."
} finally {
    Pop-Location
}
