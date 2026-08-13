param(
    [string]$PythonExecutable = "python"
)

$ErrorActionPreference = "Stop"
$ResearchRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ArtifactRoot = Join-Path $ResearchRoot "artifacts"
$ModelRoot = Join-Path $ArtifactRoot "minilm-multilingual"
$WheelRoot = Join-Path $ArtifactRoot "wheels"
$VirtualEnvironment = Join-Path $ResearchRoot ".venv"
$ModelPath = Join-Path $ModelRoot "model_qint8.onnx"
$TokenizerPath = Join-Path $ModelRoot "tokenizer.json"
$ModelRevision = "e8f8c211226b894fcb81acc59f3b34ba3efd5f42"
$ExpectedModelHash = "783fea82d71a58179b830a4dbd2d58447e640609e98eedf9ffa12622d375a672"
$ExpectedTokenizerHash = "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8"

New-Item -ItemType Directory -Force -Path $ModelRoot | Out-Null
New-Item -ItemType Directory -Force -Path $WheelRoot | Out-Null

function Get-VerifiedArtifact {
    param(
        [string]$Url,
        [string]$Destination,
        [string]$ExpectedHash
    )
    if (-not (Test-Path -LiteralPath $Destination)) {
        & curl.exe -L --fail --retry 3 --max-time 600 --output $Destination $Url
        if ($LASTEXITCODE -ne 0) { throw "Artifact download failed: $Destination" }
    }
    $ActualHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Destination).Hash.ToLowerInvariant()
    if ($ActualHash -ne $ExpectedHash) {
        throw "Artifact SHA-256 mismatch: $Destination"
    }
}

$ModelUrl = "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/$ModelRevision/onnx/model_qint8_arm64.onnx?download=true"
$TokenizerUrl = "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/$ModelRevision/tokenizer.json?download=true"
Get-VerifiedArtifact -Url $ModelUrl -Destination $ModelPath -ExpectedHash $ExpectedModelHash
Get-VerifiedArtifact -Url $TokenizerUrl -Destination $TokenizerPath -ExpectedHash $ExpectedTokenizerHash

$Packages = @(
    "onnxruntime==1.28.0",
    "tokenizers==0.23.1",
    "numpy==2.5.2",
    "flatbuffers==25.12.19",
    "packaging==26.3",
    "protobuf==7.35.1"
)

& $PythonExecutable -m pip download --only-binary=:all: --no-deps --dest $WheelRoot @Packages
if ($LASTEXITCODE -ne 0) { throw "Research wheel download failed." }

if (-not (Test-Path -LiteralPath $VirtualEnvironment)) {
    & $PythonExecutable -m venv $VirtualEnvironment
    if ($LASTEXITCODE -ne 0) { throw "Research virtual environment creation failed." }
}

$VenvPython = Join-Path $VirtualEnvironment "Scripts\python.exe"
$WheelFiles = Get-ChildItem -LiteralPath $WheelRoot -File -Filter *.whl | Sort-Object Name
& $VenvPython -m pip install --no-index --no-deps --find-links $WheelRoot @Packages
if ($LASTEXITCODE -ne 0) { throw "Offline research wheel installation failed." }

$WheelLock = @()
foreach ($Wheel in $WheelFiles) {
    $WheelLock += [ordered]@{
        filename = $Wheel.Name
        size = $Wheel.Length
        sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $Wheel.FullName).Hash.ToLowerInvariant()
    }
}
$WheelLockDocument = [ordered]@{
    schemaVersion = 1
    python = (& $VenvPython --version 2>&1 | Out-String).Trim()
    platform = "win_amd64"
    installMode = "no-index-no-deps"
    wheels = $WheelLock
}
$WheelLockDocument | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 (Join-Path $ResearchRoot "wheel-lock.json")

$ModelManifest = [ordered]@{
    schemaVersion = 1
    modelId = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    revision = $ModelRevision
    license = "Apache-2.0"
    model = [ordered]@{
        sourcePath = "onnx/model_qint8_arm64.onnx"
        localFilename = "model_qint8.onnx"
        size = (Get-Item -LiteralPath $ModelPath).Length
        sha256 = $ExpectedModelHash
    }
    tokenizer = [ordered]@{
        sourcePath = "tokenizer.json"
        localFilename = "tokenizer.json"
        size = (Get-Item -LiteralPath $TokenizerPath).Length
        sha256 = $ExpectedTokenizerHash
    }
    productLinked = $false
    heldoutAuthorized = $false
}
$ModelManifest | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 (Join-Path $ResearchRoot "model-manifest.json")

$env:HF_HUB_OFFLINE = "1"
$env:TRANSFORMERS_OFFLINE = "1"
& $VenvPython -c "import importlib.util, numpy, onnxruntime, tokenizers; assert importlib.util.find_spec('huggingface_hub') is None; assert importlib.util.find_spec('requests') is None; assert importlib.util.find_spec('httpx') is None; print('offline research imports passed', onnxruntime.__version__, tokenizers.__version__, numpy.__version__)"
if ($LASTEXITCODE -ne 0) { throw "Research environment isolation check failed." }

Write-Output "MiniLM model, tokenizer, wheels, hashes, and offline environment are ready."
