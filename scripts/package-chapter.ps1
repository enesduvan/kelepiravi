param(
    [string]$Chapter = "1",
    [string]$Out = "./chapter-$Chapter.zip"
)

$excludes = @('.git', '.github', 'node_modules', 'build', 'bin', 'obj')

$items = Get-ChildItem -Force | Where-Object { $excludes -notcontains $_.Name } | ForEach-Object { Join-Path -Path $_.FullName -ChildPath '*' }

if (-not $items) {
    Write-Error "No files to archive"
    exit 1
}

Compress-Archive -Path $items -DestinationPath $Out -Force
Write-Output "Created $Out"