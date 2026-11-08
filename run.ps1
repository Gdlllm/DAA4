param(
    [ValidateSet('run', 'check', 'bench', 'generate')]
    [string]$Action = 'run',
    [string]$StudentId = ''
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ($Action -eq 'generate') {
        if ([string]::IsNullOrWhiteSpace($StudentId)) {
            throw 'Pass your exact Moodle ID as a quoted string: .\run.ps1 generate "012345"'
        }
        & java tools/MakeVariant.java $StudentId data
        if ($LASTEXITCODE -ne 0) { throw 'Data generation failed.' }
        # The previous screenshot belongs to the previous input data.
        if (Test-Path -LiteralPath 'output/outbreak.png') {
            Move-Item -LiteralPath 'output/outbreak.png' -Destination 'output/outbreak-previous.png' -Force
        }
    }
    New-Item -ItemType Directory -Path out,output -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 --release 17 -d out @sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
    switch ($Action) {
        'check' {
            $exampleLines = @(& java tools/Check.java)
            if ($LASTEXITCODE -ne 0) { throw 'Example check failed.' }
            $exampleLines | Tee-Object -FilePath output/checks.txt
            if (-not ($exampleLines -match '^All examples pass\.')) { throw 'Examples do not match.' }
            & javac -encoding UTF-8 --release 17 -cp out -d out tests/AlgorithmTests.java
            if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
            & java -cp out AlgorithmTests | Tee-Object -FilePath output/tests.txt
            if ($LASTEXITCODE -ne 0) { throw 'Algorithm tests failed.' }
            # Check.java writes a page for the dense example; restore the current data page.
            & java -cp out Main | Tee-Object -FilePath output/results.txt
            if ($LASTEXITCODE -ne 0) { throw 'Main failed.' }
        }
        'bench' {
            & java -cp out Bench | Tee-Object -FilePath output/timing.txt
            if ($LASTEXITCODE -ne 0) { throw 'Benchmark failed.' }
        }
        default {
            & java -cp out Main | Tee-Object -FilePath output/results.txt
            if ($LASTEXITCODE -ne 0) { throw 'Main failed.' }
        }
    }
} finally {
    Pop-Location
}
