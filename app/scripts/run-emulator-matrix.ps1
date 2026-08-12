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
if (-not $androidSdk) {
    throw "Android SDK not found. Set ANDROID_HOME or install the SDK in the standard Android Studio location."
}

$adb = Join-Path $androidSdk "platform-tools\adb.exe"
$emulator = Join-Path $androidSdk "emulator\emulator.exe"
$gradleRoot = Split-Path -Parent $PSScriptRoot
$gradle = Join-Path $gradleRoot "gradlew.bat"

foreach ($required in @($adb, $emulator, $gradle)) {
    if (-not (Test-Path -LiteralPath $required)) {
        throw "Required executable not found: $required"
    }
}

$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

function Get-EmulatorSerials {
    $lines = & $adb devices
    @($lines | Select-String '^emulator-\d+\s+device$' | ForEach-Object {
        (($_.Line -split '\s+')[0]).Trim()
    })
}

function Get-AvdName {
    param([Parameter(Mandatory = $true)][string]$Serial)

    $result = & $adb -s $Serial emu avd name 2>$null
    if ($LASTEXITCODE -ne 0) { return $null }
    @($result | Where-Object { $_ -and $_ -ne "OK" }) | Select-Object -First 1
}

function Find-AvdSerial {
    param([Parameter(Mandatory = $true)][string]$AvdName)

    foreach ($serial in Get-EmulatorSerials) {
        if ((Get-AvdName -Serial $serial) -eq $AvdName) {
            return $serial
        }
    }
    return $null
}

function Wait-ForAvd {
    param(
        [Parameter(Mandatory = $true)][string]$AvdName,
        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $serial = Find-AvdSerial -AvdName $AvdName
        if ($serial) {
            $booted = (& $adb -s $serial shell getprop sys.boot_completed 2>$null).Trim()
            if ($booted -eq "1") { return $serial }
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for AVD '$AvdName'."
}

function Wait-ForDocumentProvider {
    param(
        [Parameter(Mandatory = $true)][string]$Serial,
        [Parameter(Mandatory = $true)][string]$AvdName,
        [int]$TimeoutSeconds = 60
    )

    $providerArguments = @(
        "-s", $Serial,
        "shell", "cmd", "package", "resolve-activity", "--brief",
        "-a", "android.intent.action.CREATE_DOCUMENT",
        "-t", "text/plain"
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $documentProvider = & $adb @providerArguments 2>$null
        if ($LASTEXITCODE -eq 0 -and ($documentProvider -join "`n") -notmatch "No activity found") {
            return
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "AVD '$AvdName' has no working ACTION_CREATE_DOCUMENT provider. Recreate or provision the AVD before testing Fio."
}

Push-Location $gradleRoot
try {
    if (-not $SkipBuild) {
        Write-Host "== JVM tests, lint, and isolated validation APK =="
        Invoke-Checked -FilePath $gradle -Arguments @(
            "--no-daemon",
            ":mobile:testDebugUnitTest",
            ":mobile:lintDebug",
            ":mobile:assembleValidation",
            ":mobile:assembleDebugAndroidTest"
        )
    }

    foreach ($avdName in $AvdNames) {
        $serial = Find-AvdSerial -AvdName $avdName
        $startedHere = $false

        if (-not $serial) {
            Write-Host "== Starting $avdName =="
            $startArguments = @{
                FilePath = $emulator
                ArgumentList = @(
                    "-avd", $avdName,
                    "-no-snapshot-load",
                    "-no-snapshot-save",
                    "-no-boot-anim",
                    "-gpu", "auto"
                )
                WindowStyle = "Hidden"
            }
            Start-Process @startArguments | Out-Null
            $startedHere = $true
        }

        try {
            $serial = Wait-ForAvd -AvdName $avdName
            $api = (& $adb -s $serial shell getprop ro.build.version.sdk).Trim()
            Wait-ForDocumentProvider -Serial $serial -AvdName $avdName
            Write-Host "== Instrumented tests: $avdName ($serial, API $api) =="
            $env:ANDROID_SERIAL = $serial
            Invoke-Checked -FilePath $gradle -Arguments @(
                "--no-daemon",
                ":mobile:connectedDebugAndroidTest"
            )
        } finally {
            Remove-Item Env:ANDROID_SERIAL -ErrorAction SilentlyContinue
            if ($startedHere -and -not $KeepEmulators -and $serial) {
                Write-Host "== Stopping $avdName =="
                & $adb -s $serial emu kill | Out-Null
            }
        }
    }

    Write-Host "M1 emulator matrix completed successfully."
} finally {
    Pop-Location
}
