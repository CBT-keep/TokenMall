$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$gitDir = Join-Path $repoRoot ".git"
if (-not (Test-Path -LiteralPath $gitDir)) {
    throw "This project is not a Git repository. Run git init first."
}

$hookDir = Join-Path $gitDir "hooks"
New-Item -ItemType Directory -Path $hookDir -Force | Out-Null

$hookPath = Join-Path $hookDir "post-commit"
$hookContent = @'
#!/bin/sh
subject=$(git log -1 --pretty=%s)
if [ "$subject" = "chore: record knowledge session" ]; then
  extra_args=""
else
  extra_args="-AppendSession -SessionTitle Git_Commit"
fi

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "scripts/knowledge/Update-KnowledgeIndex.ps1" $extra_args
else
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "scripts/knowledge/Update-KnowledgeIndex.ps1" $extra_args
fi
'@

Set-Content -LiteralPath $hookPath -Value $hookContent -Encoding ASCII
Write-Host "Installed post-commit hook: $hookPath"
