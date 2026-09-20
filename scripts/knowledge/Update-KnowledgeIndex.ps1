param(
    [switch]$AppendSession,
    [string]$SessionTitle = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$vaultRoot = Join-Path $repoRoot "knowledge-vault"
$homeRoot = Join-Path $vaultRoot "00-Home"
$courseRoot = Join-Path $vaultRoot "02-Course"
$logRoot = Join-Path $vaultRoot "09-Logs"
$lf = "`n"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Write-ManagedSection {
    param(
        [string]$Path,
        [string]$StartMarker,
        [string]$EndMarker,
        [string]$Content
    )

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $resolvedPath)) {
        throw "Managed file does not exist: $resolvedPath"
    }

    $raw = Get-Content -LiteralPath $resolvedPath -Raw -Encoding UTF8
    $pattern = "(?s)" + [regex]::Escape($StartMarker) + ".*?" + [regex]::Escape($EndMarker)
    $replacement = $StartMarker + $lf + $Content.Trim() + $lf + $EndMarker

    if ($raw -match $pattern) {
        $updated = [regex]::Replace($raw, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{
            param($match)
            return $replacement
        })
    }
    else {
        $updated = $raw.TrimEnd() + $lf + $lf + $replacement + $lf
    }

    $normalized = $updated.TrimEnd() + $lf
    [System.IO.File]::WriteAllText($resolvedPath, $normalized, $utf8NoBom)
}

$courseFiles = @()
if (Test-Path -LiteralPath $courseRoot) {
    $courseFiles = Get-ChildItem -LiteralPath $courseRoot -Filter "Day-*.md" -File |
        Sort-Object Name
}

$courseLines = New-Object System.Collections.Generic.List[string]
foreach ($file in $courseFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    $title = if ($content -match "(?m)^#\s+(.+)$") { $Matches[1].Trim() } else { $file.BaseName }
    $day = if ($content -match "(?m)^day:\s*(\d+)\s*$") { [int]$Matches[1] } else { 0 }
    $status = if ($content -match "(?m)^status:\s*(.+?)\s*$") { $Matches[1].Trim() } else { "planned" }
    $linkName = $file.BaseName
    $courseLines.Add("| $day | [[$linkName|$title]] | $status |")
}

if ($courseLines.Count -gt 0) {
    $courseContent = @(
        "| 天数 | 课程 | 状态 |",
        "| --- | --- | --- |"
    ) + $courseLines
}
else {
    $courseContent = @("尚未创建课程笔记。")
}

$courseSection = @{
    Path = (Join-Path $homeRoot "Course Index.md")
    StartMarker = "<!-- AUTO-COURSE-INDEX:START -->"
    EndMarker = "<!-- AUTO-COURSE-INDEX:END -->"
    Content = ($courseContent -join $lf)
}
Write-ManagedSection @courseSection

$gitLines = New-Object System.Collections.Generic.List[string]
Push-Location $repoRoot
try {
    $insideGit = (& git rev-parse --is-inside-work-tree 2>$null)
    if ($LASTEXITCODE -eq 0 -and $insideGit -eq "true") {
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        $logOutput = & git log --date=short --pretty=format:"%h|%ad|%s" -n 100 2>$null
        $gitLogExitCode = $LASTEXITCODE
        $ErrorActionPreference = $previousErrorAction

        if ($gitLogExitCode -eq 0 -and $logOutput) {
            $gitLines.Add("| 提交 | 日期 | 说明 |")
            $gitLines.Add("| --- | --- | --- |")
            foreach ($line in $logOutput) {
                $parts = $line -split "\|", 3
                if ($parts.Count -eq 3) {
                    if ($parts[2] -match "^chore: record knowledge session") {
                        continue
                    }
                    $gitLines.Add("| $($parts[0]) | $($parts[1]) | $($parts[2]) |")
                }
            }
        }
    }
}
finally {
    Pop-Location
}

if ($gitLines.Count -eq 0) {
    $gitLines.Add("项目尚未产生 Git 提交。")
}

$developmentSection = @{
    Path = (Join-Path $homeRoot "Development Log.md")
    StartMarker = "<!-- AUTO-DEVELOPMENT-LOG:START -->"
    EndMarker = "<!-- AUTO-DEVELOPMENT-LOG:END -->"
    Content = ($gitLines -join $lf)
}
Write-ManagedSection @developmentSection

$openItems = New-Object System.Collections.Generic.List[string]
foreach ($file in $courseFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    $lineNumber = 0
    foreach ($line in ($content -split "`r?`n")) {
        $lineNumber++
        if ($line -match "^\s*-\s*\[\s*\]\s*(.+)$") {
            $item = $Matches[1].Trim()
            $openItems.Add("- [[$($file.BaseName)]] line $lineNumber - $item")
        }
        elseif ($line -match "LEARNING-TODO|TODO\(|^\s*TODO:") {
            $openItems.Add("- [[$($file.BaseName)]] line $lineNumber - $($line.Trim())")
        }
    }
}

if ($openItems.Count -eq 0) {
    $openItems.Add("尚未扫描到未完成项。")
}

$questionsSection = @{
    Path = (Join-Path $homeRoot "Open Questions.md")
    StartMarker = "<!-- AUTO-OPEN-QUESTIONS:START -->"
    EndMarker = "<!-- AUTO-OPEN-QUESTIONS:END -->"
    Content = ($openItems -join $lf)
}
Write-ManagedSection @questionsSection

if ($AppendSession) {
    if ([string]::IsNullOrWhiteSpace($SessionTitle)) {
        $SessionTitle = "开发会话"
    }

    $shortHash = ""
    $subject = ""
    Push-Location $repoRoot
    try {
        $shortHash = (& git rev-parse --short HEAD 2>$null)
        $subject = (& git log -1 --pretty=format:"%s" 2>$null)
    }
    finally {
        Pop-Location
    }

    $entryLines = @(
        "",
        "## $SessionTitle",
        "",
        "- 时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
        "- 提交：$shortHash",
        "- 说明：$subject"
    )

    $sessionsPath = Join-Path $logRoot "Development Sessions.md"
    $sessionsRaw = Get-Content -LiteralPath $sessionsPath -Raw -Encoding UTF8
    $startMarker = "<!-- AUTO-SESSIONS:START -->"
    $endMarker = "<!-- AUTO-SESSIONS:END -->"
    $pattern = "(?s)" + [regex]::Escape($startMarker) + "(.*?)" + [regex]::Escape($endMarker)
    $existing = ""
    if ($sessionsRaw -match $pattern) {
        $existing = $Matches[1].Trim()
    }

    if ($existing -eq "") {
        $newContent = ($entryLines -join $lf).Trim()
    }
    else {
        $newContent = ($existing + $lf + ($entryLines -join $lf)).Trim()
    }

    $sessionSection = @{
        Path = $sessionsPath
        StartMarker = $startMarker
        EndMarker = $endMarker
        Content = $newContent
    }
    Write-ManagedSection @sessionSection
}

Write-Host "Knowledge index updated."
