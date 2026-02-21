# Offline / slow-network dependency downloads

When Gradle fails to download dependencies (e.g. "Read timed out" for `material-icons-extended`), use the download script to fetch them into a local Maven repo.

## Download methods (in order of use)

1. **aria2** at `C:\Users\swapnil\Downloads\aria2-1.37.0-win-64bit-build1\aria2c.exe`  
   Multi-connection, resume; used when Gradle downloads fail due to timeouts.

2. **curl** or **Invoke-WebRequest**  
   Used if aria2 is not found, or if you pass `-ForceCurl`.

To force curl/PowerShell only (skip aria2):

```powershell
.\scripts\download-offline-deps.ps1 -ForceCurl
```

## Steps

1. **Run the download script** (from project root):

   ```powershell
   .\scripts\download-offline-deps.ps1
   ```

   The script uses aria2 (or curl / Invoke-WebRequest) with retries and long timeouts and writes files into `local-maven-repo/`.

2. **Build as usual:**

   ```powershell
   .\gradlew.bat assembleDebug
   ```

   Gradle is configured to look at `local-maven-repo` first for `androidx.compose.material` artifacts, so it will use the pre-downloaded files.

## Adding more artifacts

Edit `scripts/download-offline-deps.ps1` and add entries to the `$Artifacts` array with `GroupPath`, `Version`, and `ArtifactId` for any other failing dependencies (use the same Maven path structure as in the Google Maven URL).
