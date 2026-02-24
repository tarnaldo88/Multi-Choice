$ErrorActionPreference = 'Stop'

Write-Host 'Stopping Gradle daemons...'
& .\gradlew.bat --stop | Out-Host

Write-Host 'Stopping stale Java processes (if any)...'
Get-Process java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host 'Removing local build outputs...'
if (Test-Path .\app\build) { Remove-Item -Recurse -Force .\app\build }
if (Test-Path .\.gradle) { Remove-Item -Recurse -Force .\.gradle }

Write-Host 'Building debug APK...'
& .\gradlew.bat clean :app:assembleDebug --no-daemon --no-parallel --stacktrace

Write-Host 'APK path:'
Write-Host '.\app\build\outputs\apk\debug\app-debug.apk'
