$ErrorActionPreference = "Stop"
$ResearchRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$VenvPython = Join-Path $ResearchRoot ".venv\Scripts\python.exe"

if (-not (Test-Path -LiteralPath $VenvPython)) {
    throw "Run prepare_minilm.ps1 first."
}

$env:HF_HUB_OFFLINE = "1"
$env:TRANSFORMERS_OFFLINE = "1"
$env:NO_PROXY = "*"
& $VenvPython (Join-Path $ResearchRoot "harness\minilm_adapter.py")
if ($LASTEXITCODE -ne 0) { throw "MiniLM development rehearsal failed." }

Write-Output "MiniLM development-only offline rehearsal passed."
