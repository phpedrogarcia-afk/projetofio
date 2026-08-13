param(
    [string]$PythonExecutable = "python"
)

$ErrorActionPreference = "Stop"
$ResearchRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

& $PythonExecutable (Join-Path $ResearchRoot "corpus\generate_corpus.py")
if ($LASTEXITCODE -ne 0) { throw "M4 corpus generation failed." }

& $PythonExecutable (Join-Path $ResearchRoot "corpus\validate_corpus.py")
if ($LASTEXITCODE -ne 0) { throw "M4 corpus validation failed." }

& $PythonExecutable -m unittest discover -s (Join-Path $ResearchRoot "harness") -p "test_*.py" -v
if ($LASTEXITCODE -ne 0) { throw "M4 harness tests failed." }

& $PythonExecutable (Join-Path $ResearchRoot "harness\benchmark.py")
if ($LASTEXITCODE -ne 0) { throw "M4 lexical rehearsal failed." }

Write-Output "M4 offline corpus, blind packets, tests, and lexical rehearsal passed."
