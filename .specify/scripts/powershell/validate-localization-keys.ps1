# Localization Key Validation Script
# Feature: 008-missing-localization-keys
# Scans Java code for getMessage() calls and validates against YAML files

param(
    [string]$SourcePath = "src/main/java",
    [string]$ResourcePath = "src/main/resources",
    [string]$Language = "ru",
    [switch]$Strict,
    [switch]$Json,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

# Color output helpers
function Write-Success { param($msg) Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-ErrorCustom { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }
function Write-WarningCustom { param($msg) Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Info { param($msg) if ($Verbose) { Write-Host "[INFO] $msg" -ForegroundColor Cyan } }

# === STEP 1: Find all Java files ===
Write-Info "Scanning Java source files in $SourcePath..."
$javaFiles = Get-ChildItem -Path $SourcePath -Filter "*.java" -Recurse -ErrorAction SilentlyContinue

if (-not $javaFiles) {
    Write-ErrorCustom "No Java files found in $SourcePath"
    exit 1
}

Write-Info "Found $($javaFiles.Count) Java files"

# === STEP 2: Extract getMessage() calls using regex ===
Write-Info "Extracting getMessage() calls..."
$discoveredKeys = @{}

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Match: getMessage(player, "key") or getMessage("lang", "key") or getMessage("key")
    $patterns = @(
        'getMessage\s*\(\s*[^,]+,\s*"([^"]+)"',
        'getMessage\s*\(\s*"([^"]+)"\s*\)'
    )
    
    foreach ($pattern in $patterns) {
        $matches = [regex]::Matches($content, $pattern)
        
        foreach ($match in $matches) {
            $key = $match.Groups[1].Value
            if (-not $discoveredKeys.ContainsKey($key)) {
                $discoveredKeys[$key] = @{
                    key = $key
                    files = @()
                }
            }
            $relativePath = $file.FullName.Replace($PWD.Path + "\", "")
            if ($discoveredKeys[$key].files -notcontains $relativePath) {
                $discoveredKeys[$key].files += $relativePath
            }
        }
    }
}

Write-Info "Discovered $($discoveredKeys.Count) unique localization keys"

# === STEP 3: Load YAML files ===
Write-Info "Loading YAML localization files..."

function Parse-YamlKeys {
    param([string]$filePath)
    
    if (-not (Test-Path $filePath)) {
        Write-WarningCustom "YAML file not found: $filePath"
        return @{}
    }
    
    $keys = @{}
    $lines = Get-Content $filePath
    $currentPrefix = @()
    
    foreach ($line in $lines) {
        # Skip comments and empty lines
        if ($line -match '^\s*#' -or $line -match '^\s*$') { continue }
        
        # Detect indentation level
        if ($line -match '^(\s*)([^:]+):\s*(.*)$') {
            $indent = $matches[1].Length
            $key = $matches[2].Trim()
            $value = $matches[3].Trim()
            
            # Handle flat-format keys (e.g., "act5.too_far: value")
            if ($indent -eq 0 -and $key.Contains('.')) {
                # This is a flat key - use it directly
                $keys[$key] = $value
                continue
            }
            
            $level = [math]::Floor($indent / 2)
            
            # Adjust prefix stack
            if ($level -lt $currentPrefix.Count) {
                $currentPrefix = $currentPrefix[0..($level-1)]
            }
            
            # Build full key path
            if ($value -eq '' -or $value -eq '|' -or $value -eq '>') {
                # This is a parent key
                if ($level -ge $currentPrefix.Count) {
                    $currentPrefix += $key
                } else {
                    $currentPrefix[$level] = $key
                    $currentPrefix = $currentPrefix[0..$level]
                }
            } else {
                # This is a leaf key with value
                $fullKey = ($currentPrefix + $key) -join '.'
                $keys[$fullKey] = $value
            }
        }
    }
    
    return $keys
}

$primaryFile = Join-Path $ResourcePath "messages_$Language.yml"
$secondaryFile = Join-Path $ResourcePath "messages_en.yml"

$primaryKeys = Parse-YamlKeys -filePath $primaryFile
$secondaryKeys = Parse-YamlKeys -filePath $secondaryFile

Write-Info "Primary ($Language): $($primaryKeys.Count) keys"
Write-Info "Secondary (en): $($secondaryKeys.Count) keys"

# === STEP 4: Validate keys ===
Write-Info "Validating discovered keys against YAML files..."

$missingPrimary = @()
$missingSecondary = @()
$foundKeys = @()

foreach ($keyData in $discoveredKeys.Values) {
    $key = $keyData.key
    $files = $keyData.files
    
    $inPrimary = $primaryKeys.ContainsKey($key)
    $inSecondary = $secondaryKeys.ContainsKey($key)
    
    if (-not $inPrimary) {
        $missingPrimary += @{
            key = $key
            files = $files
        }
    }
    
    if (-not $inSecondary) {
        $missingSecondary += @{
            key = $key
            files = $files
        }
    }
    
    if ($inPrimary -and $inSecondary) {
        $foundKeys += $key
    }
}

# === STEP 5: Report results ===
if ($Json) {
    $result = @{
        summary = @{
            totalKeysFound = $discoveredKeys.Count
            validKeys = $foundKeys.Count
            missingPrimary = $missingPrimary.Count
            missingSecondary = $missingSecondary.Count
        }
        missingPrimary = $missingPrimary
        missingSecondary = $missingSecondary
        validKeys = $foundKeys
    }
    
    $result | ConvertTo-Json -Depth 10
} else {
    Write-Host "`n=== Localization Validation Report ===" -ForegroundColor Cyan
    Write-Host "Total keys discovered: $($discoveredKeys.Count)"
    Write-Host "Valid keys (in both files): $($foundKeys.Count)"
    Write-Host "Missing primary ($Language): $($missingPrimary.Count)"
    Write-Host "Missing secondary (en): $($missingSecondary.Count)"
    
    if ($missingPrimary.Count -gt 0) {
        Write-Host "`nMissing Primary Language Keys ($Language):" -ForegroundColor Red
        foreach ($item in $missingPrimary) {
            Write-Host "  - $($item.key)" -ForegroundColor Red
            foreach ($file in $item.files) {
                Write-Host "    from: $file" -ForegroundColor DarkGray
            }
        }
    }
    
    if ($missingSecondary.Count -gt 0) {
        Write-Host "`nMissing Secondary Language Keys (en):" -ForegroundColor Yellow
        foreach ($item in $missingSecondary) {
            Write-Host "  - $($item.key)" -ForegroundColor Yellow
            foreach ($file in $item.files) {
                Write-Host "    from: $file" -ForegroundColor DarkGray
            }
        }
    }
    
    if ($missingPrimary.Count -eq 0 -and $missingSecondary.Count -eq 0) {
        Write-Success "All localization keys are present!"
    }
}

# === STEP 6: Exit code ===
if ($missingPrimary.Count -gt 0) {
    Write-Host "`n[FAILED] Missing primary language keys" -ForegroundColor Red
    exit 1
}

if ($Strict -and $missingSecondary.Count -gt 0) {
    Write-Host "`n[FAILED] Missing secondary language keys (strict mode)" -ForegroundColor Red
    exit 1
}

if ($missingSecondary.Count -gt 0) {
    Write-WarningCustom "`nBUILD WARNING: Missing secondary language keys"
    exit 0
}

Write-Success "`n[PASSED] All localization keys validated"
exit 0
