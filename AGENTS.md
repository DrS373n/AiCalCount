# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

AiCalCount is a native Android app (Kotlin / Jetpack Compose) for AI-powered calorie tracking. There is no backend; all services are remote APIs (Spoonacular, Google Gemini).

### Prerequisites

- **JDK 21** – required by the Gradle daemon toolchain (`gradle/gradle-daemon-jvm.properties`).
- **Android SDK** at `$ANDROID_HOME` with `platforms;android-36`, `build-tools;36.0.0`, and `platform-tools`.
- A `local.properties` file in the repo root pointing `sdk.dir` to the Android SDK path.
- Environment variables `JAVA_HOME` and `ANDROID_HOME` must be set (configured in `~/.bashrc`).

### Common commands

| Task | Command |
|---|---|
| Build debug APK | `./gradlew assembleDebug` |
| Run unit tests | `./gradlew testDebugUnitTest` |
| Lint | `./gradlew lintDebug` |
| Clean build | `./gradlew clean assembleDebug` |
| Full build + install + launch | `./gradlew deploy` (requires connected device/emulator) |

### Gotchas

- `assembleDebug` has a `bumpVersion` finalizer that auto-increments `app/version.properties`. After building, revert this file (`git checkout -- app/version.properties`) to avoid committing spurious version bumps.
- `gradle.properties` contains Windows-specific `systemProp.org.gradle.internal.downloader.*` properties pointing to `aria2c.exe`. These are silently ignored on Linux and do not affect the build.
- Running `lintDebug` immediately after `assembleDebug` (without `clean`) can fail due to stale manifest intermediates from the `bumpVersion` finalizer. Use `./gradlew clean lintDebug` if lint fails with a missing `AndroidManifest.xml` error.
- The Gemini API key is set to `"YOUR_API_KEY"` placeholder in `app/build.gradle.kts`. AI features require a real key via `BuildConfig.GEMINI_API_KEY`.
- No Android emulator is set up in the cloud environment. Unit tests and lint run without a device; instrumented tests (`connectedAndroidTest`) require an emulator or device.
