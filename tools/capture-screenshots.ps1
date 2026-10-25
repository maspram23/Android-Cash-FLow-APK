# Capture PAKU UI screenshots (Indonesian + English) from a running emulator/device.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File tools/capture-screenshots.ps1
#
# Output:
#   docs/screenshots/*.png      -> Indonesian set
#   docs/screenshots/en/*.png   -> English set
#
# The emulator's system locale is en-US, so `LanguageMode.SYSTEM` starts the app
# in English. The script captures the English set first, then switches the app
# language to Indonesian from the Settings screen and captures the Indonesian
# set. Labels are looked up through `uiautomator dump`, so no coordinates are
# hardcoded (only swipe gestures use screen geometry).

param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Package = "com.paku.keuangan",
    [string]$Apk = "app/build/outputs/apk/debug/app-debug.apk",
    [string]$OutDir = "docs/screenshots"
)

$ErrorActionPreference = "Continue"

function Invoke-Adb {
    param([string[]]$AdbArgs)
    & $Adb @AdbArgs
}

function Wait-ForDevice {
    for ($i = 0; $i -lt 60; $i++) {
        $state = (& $Adb get-state 2>&1 | Out-String).Trim()
        if ($state -eq "device") {
            $boot = (& $Adb shell getprop sys.boot_completed 2>&1 | Out-String).Trim()
            if ($boot -eq "1") { return }
        }
        Start-Sleep -Seconds 3
    }
    throw "Device never reached sys.boot_completed=1"
}

function Save-Screenshot {
    param([string]$Name, [string]$Sub = "")
    $dir = if ($Sub) { Join-Path $OutDir $Sub } else { $OutDir }
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $target = (Resolve-Path $dir).Path + "\$Name.png"
    # cmd handles raw byte redirection; PowerShell's `>` would re-encode.
    & cmd /c "`"$Adb`" exec-out screencap -p > `"$target`""
    Write-Host "saved $target"
}

function Get-UiXml {
    Invoke-Adb @("shell", "uiautomator", "dump", "/sdcard/ui.xml") | Out-Null
    return (Invoke-Adb @("shell", "cat", "/sdcard/ui.xml") | Out-String)
}

function Get-LabelPoint {
    param([string]$Label)
    $xml = Get-UiXml
    $escaped = [regex]::Escape($Label)
    $found = [regex]::Matches(
        $xml,
        "(?:text|content-desc)=`"$escaped`"[^>]*bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`""
    )
    foreach ($m in $found) {
        $top = [int]$m.Groups[2].Value
        $bottom = [int]$m.Groups[4].Value
        if ($top -ge 0 -and $bottom -le $H) {
            return @{
                x = [int](([int]$m.Groups[1].Value + [int]$m.Groups[3].Value) / 2)
                y = [int](($top + $bottom) / 2)
            }
        }
    }
    return $null
}

function Scroll-Up {
    # reveals content below the fold
    Invoke-Adb @("shell", "input", "swipe", "$([int]($W / 2))", "$([int]($H * 0.80))", "$([int]($W / 2))", "$([int]($H * 0.35))", "350") | Out-Null
    Start-Sleep -Seconds 2
}

function Scroll-Down {
    # reveals content above the fold
    Invoke-Adb @("shell", "input", "swipe", "$([int]($W / 2))", "$([int]($H * 0.35))", "$([int]($W / 2))", "$([int]($H * 0.80))", "350") | Out-Null
    Start-Sleep -Seconds 2
}

function Scroll-To-Top {
    param([int]$Times = 8)
    for ($i = 0; $i -lt $Times; $i++) { Scroll-Down }
}

function Scroll-Until-Visible {
    param([string]$Label, [int]$MaxScrolls = 8)
    for ($i = 0; $i -le $MaxScrolls; $i++) {
        if (Get-LabelPoint $Label) { return $true }
        Scroll-Up
    }
    Write-Warning "label never became visible: $Label"
    return $false
}

function Wait-Label {
    param([string]$Label, [int]$Timeout = 25)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        if (Get-LabelPoint $Label) { return $true }
        Start-Sleep -Milliseconds 700
    }
    Write-Warning "label never appeared: $Label"
    return $false
}

function Tap-Label {
    param([string]$Label, [int]$Timeout = 22, [switch]$ScrollUp)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        $point = Get-LabelPoint $Label
        if ($point) {
            Invoke-Adb @("shell", "input", "tap", "$($point.x)", "$($point.y)") | Out-Null
            Start-Sleep -Milliseconds 900
            return $true
        }
        if ($ScrollUp) { Scroll-Up }
        Start-Sleep -Milliseconds 600
    }
    Write-Warning "label not found: $Label"
    return $false
}

function Start-Fresh {
    Invoke-Adb @("shell", "pm", "clear", $Package) | Out-Null
    Start-Sleep -Milliseconds 800
    Invoke-Adb @("shell", "am", "start", "-n", "$Package/.MainActivity") | Out-Null
    # Cold start is ~5 s and the in-app splash adds ~1.6 s.
    Start-Sleep -Seconds 9
}

function Assert-PakuForeground {
    $pkg = ""
    for ($i = 0; $i -lt 5; $i++) {
        $xml = Get-UiXml
        $match = [regex]::Match($xml, "package=`"([^`"]+)`"")
        $pkg = if ($match.Success) { $match.Groups[1].Value } else { "" }
        if ($pkg -eq $Package) { return }
        if ($pkg -eq "") {
            # No accessibility node: a system dialog (e.g. a SystemUI ANR after a
            # cold boot) usually owns the focus. Dismiss it and retry.
            Invoke-Adb @("shell", "input", "keyevent", "4") | Out-Null
            Invoke-Adb @("shell", "am", "broadcast", "-a", "android.intent.action.CLOSE_SYSTEM_DIALOGS") | Out-Null
        }
        Start-Sleep -Seconds 3
        Invoke-Adb @("shell", "am", "start", "-n", "$Package/.MainActivity") | Out-Null
        Start-Sleep -Seconds 4
    }
    throw "Expected $Package in the foreground but found '$pkg'"
}

Wait-ForDevice

# Screen geometry for swipe gestures.
$sizeLine = (Invoke-Adb @("shell", "wm", "size") | Out-String)
$wm = [regex]::Match($sizeLine, "(\d+)x(\d+)")
$W = [int]$wm.Groups[1].Value
$H = [int]$wm.Groups[2].Value
Write-Host "screen ${W}x${H}"

# 0. Install the freshest debug build.
$apkPath = (Resolve-Path $Apk).Path
Write-Host "installing $apkPath"
$install = (Invoke-Adb @("install", "-r", $apkPath) 2>&1 | Out-String).Trim()
Write-Host "  $install"
if ($install -notmatch "Success") { throw "APK install failed: $install" }

# ===========================================================================
# English set (system locale is en-US, so SYSTEM resolves to English)
# ===========================================================================

Start-Fresh
Assert-PakuForeground
# The in-app splash lasts ~1.6 s; wait for the onboarding CTA before capturing.
$null = Wait-Label "Get Started"
Save-Screenshot "01-onboarding" "en"

Tap-Label "Get Started" | Out-Null
Start-Sleep -Seconds 4
Assert-PakuForeground
Save-Screenshot "02-home" "en"

Scroll-Up
Save-Screenshot "02b-home-chart" "en"
Scroll-Down

Tap-Label "Statistics" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "03-statistics" "en"

Tap-Label "Bills" | Out-Null
Start-Sleep -Seconds 2
Save-Screenshot "03b-statistics-category" "en"

Tap-Label "Transactions" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "04-transactions" "en"

Tap-Label "Add transaction" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "05-add-transaction" "en"

Tap-Label "Close" | Out-Null
Start-Sleep -Seconds 1
Tap-Label "Settings" | Out-Null
Start-Sleep -Seconds 4
Save-Screenshot "06-settings" "en"

$null = Scroll-Until-Visible "English"
Save-Screenshot "07-settings-language" "en"

$null = Scroll-Until-Visible "Sigit Pramono"
Save-Screenshot "08-about" "en"

# ===========================================================================
# Switch the app language to Indonesian from inside Settings
# ===========================================================================

Scroll-To-Top
if (-not (Tap-Label "Indonesian" -ScrollUp -Timeout 15)) {
    throw "could not switch the app language to Indonesian"
}
Start-Sleep -Seconds 3

# ===========================================================================
# Indonesian set
# ===========================================================================

Tap-Label "Beranda" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "02-beranda"

Scroll-Up
Save-Screenshot "02b-beranda-grafik"
Scroll-Down

Tap-Label "Statistik" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "03-statistik"

Tap-Label "Tagihan" | Out-Null
Start-Sleep -Seconds 2
Save-Screenshot "03b-statistik-kategori"

Tap-Label "Transaksi" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "04-transaksi"

Tap-Label "Catat transaksi" | Out-Null
Start-Sleep -Seconds 3
Save-Screenshot "05-catat-transaksi"

Tap-Label "Tutup" | Out-Null
Start-Sleep -Seconds 1
Tap-Label "Atur" | Out-Null
Start-Sleep -Seconds 4
Save-Screenshot "06-pengaturan"

$null = Scroll-Until-Visible "Indonesia"
Save-Screenshot "07-pengaturan-bahasa"

$null = Scroll-Until-Visible "Sigit Pramono"
Save-Screenshot "08-tentang-aplikasi"

Write-Host "done"
