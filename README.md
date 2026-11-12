# HyperChange Lite

A focused fork of [HyperChanger](https://github.com/ColdP/HyperChanger), maintained
by [pku-zhb](https://github.com/pku-zhb). Package: `com.zhuhuibin.hyperchange.lite`.

The retained scope is native soft-glass tuning, notification materials and
interactions, Control Center corners, volume controls, Personal Assistant widgets
and the SecurityCenter sidebar. Local interactive previews use the same tuning and
motion models. OEM material support is identified separately from a visual fallback.

This fork does not bundle a launcher, music/lyrics engine, iOS notification-center
replacement, camera module, third-party navigation adaptations, OCR, or prebuilt
native patch library. HMC Duo remains the owner of its lockscreen content.
See [deferred ownership](design/DEFERRED.md), [recovery boundaries](design/RECOVERY.md)
and host validation.
The settings App uses [Miuix](https://github.com/compose-miuix-ui/miuix) with feature
navigation, grouped controls and contextual previews. See the [App design](design/APP.md).

Preview 1 had a plugin-discovery startup regression. Preview 2 contains a guarded,
deferred discovery repair; read the [startup recovery boundary](design/STARTUP_SAFETY.md)
before any device scope trial. Host verification does not establish device recovery.

## Build

Requires JDK 21+, Android SDK/build tools 37 and libxposed API 102.

```sh
export DEV_VOLUME=/path/to/build-cache   # required: writable directory for outputs and caches
export ANDROID_HOME=/path/to/android-sdk # SDK with API 37 (default: $DEV_TOOLS_DIR/android-sdk)
tools/build.sh :app:testDebugUnitTest :app:assembleDebug
```

`tools/build.sh` keeps every build output, Gradle cache and temporary file outside the
source tree. Environment variables:

| Variable | Purpose |
| --- | --- |
| `DEV_VOLUME` | Required. Existing, writable directory for build outputs and caches. |
| `DEV_TOOLS_DIR` | Optional. Shared tools directory holding `android-sdk` and `gradle` (default `$DEV_VOLUME/tools`). |
| `LITE_DEV_CACHE` | Optional. Per-worktree cache directory inside `DEV_VOLUME` (default `$DEV_VOLUME/hc-lite`). |
| `LITE_GRADLE_MAX_WORKERS` | Optional. Gradle worker limit (default 4). |
| `JAVA_HOME`, `ANDROID_HOME`, `GRADLE_USER_HOME` | Standard tool locations; override the defaults above. |

Without the helper script, set `JAVA_HOME`, `ANDROID_HOME` and `LITE_BUILD_ROOT`, then run
`./gradlew -I tools/gradle-init.gradle :app:testDebugUnitTest :app:assembleDebug`.

To retain a verified APK, run `python3 tools/archive-apk.py` from a clean, pushed
revision. It checks the APK identity and signature and the embedded source revision,
stores `hc-lite-<version>.apk` with a receipt under `releases/local/`, and copies an
identical APK into `$APK_ARCHIVE_DIR` (default `./apk-archive`, which must exist; or pass
`--archive-dir`). It also reads `DEV_VOLUME` (location of the built APK), `ANDROID_HOME`
and `JAVA_HOME`. A different build never replaces an archived version.

## Preview status

This is a source and host-validation preview. No phone installation or device
acceptance is implied. Features are individually configurable; new system behavior
starts disabled until selected. Presets cover all exposed settings and validate
the full document before changing state. The app never restarts hooked packages.

Enable only the four declared scopes when testing: SystemUI, its Control Center
plugin, Personal Assistant and SecurityCenter. No framework or launcher scope is
required. Do not enable equivalent HyperLight behavior at the same time.

## Attribution

Derived from HyperChanger commit `5d89372`, with original copyright and licensing
retained. Reconstructed effects use behavioral evidence rather than redistributed
HyperLight assets or binaries. This fork is distributed under AGPL-3.0; see
[LICENSE](LICENSE) and [NOTICE](NOTICE).
