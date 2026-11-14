# hc-lite

A focused fork of [HyperChanger](https://github.com/ColdP/HyperChanger), maintained
at [pku-zhb/hc-lite](https://github.com/pku-zhb/hc-lite). Package:
`com.zhuhuibin.hyperchange.lite`.

Version 0.1.8 restores the native flat brightness/volume fill boundary while
keeping the enclosing track corners adjustable. Lockscreen notification material
now follows the native binder's live heads-up flows rather than a lingering
disappearance-animation flag; media material also refreshes after keyguard/AOD
scene changes. Material and default-off animation permissions remain separate.
An explicit read-only material dump distinguishes scope, binder and setter
results. The App restores a collapsing animated About header, colored artwork
and text, liquid navigation and floating-background blur without a second
opaque navigation layer. Existing settings and backup compatibility are retained.
See [lockscreen material tracing](design/LOCKSCREEN_SCOPES.md),
[slider corners](design/CONTROL_SLIDER_RADIUS.md) and
host validation. The existing Debug signing identity is
retained; formal production signing is not configured. Phone appearance and
transition behavior remain device acceptance; this delivery does not install or
reload any phone scope.

Version 0.1.7 separates lockscreen notification material from default-off
lockscreen animations. Plain native lockscreen rows can acquire their current
keyguard glass recipe; settled screen/wake state and current native ownership
govern acquisition and release. Control-center sliders now cover independent
track/fill corners and native resource replacements, including fold qualifiers.
The App adds six theme modes, floating navigation, App-local blur, a complete
About/credits/offline-licence structure and a companion link. Material slider
windows include all supplied presets while retaining legal import ranges.
See [lockscreen scopes](design/LOCKSCREEN_SCOPES.md),
[slider corners](design/CONTROL_SLIDER_RADIUS.md) and
host checks. Pull-down background zoom remains
[unimplemented](design/PULLDOWN_ZOOM.md): the verified candidate scales keyguard
content, not the requested backdrop. This release uses no device connection.

Version 0.1.6 targets the actual whole-window compositor and native backdrop
controllers, with independent blur and dimming controls and a full-panel preview.
Card glass remains separate. Exact current native writers restore owned backdrop
changes at the shade/heads-up handoff instead of replaying a previous scene.
Settings now group scopes, hide inactive children, fold advanced optics/curves,
show necessary prerequisites and baselines, and connect local preview wallpapers.
The optional **顶部通知柔光玻璃** switch restores upstream's separate banner-glass
style; it defaults off and requires card tuning plus the notification range.
Native banner tint tuning and scene-specific previews are corrected too. Existing
setting keys, defaults and preset values remain upgrade-compatible. See
[settings presentation](design/SETTINGS_UX.md),
[whole-window ownership](design/WHOLE_WINDOW_BACKDROP_016.md) and
[banner materials](design/BANNER_MATERIAL.md). About also provides the maintainer's
WeChat donation image with gallery saving. GPU rendering still needs device acceptance.

Version 0.1.5 removes generated-view-ID exception storms, coalesces slider writes,
reads each material snapshot once, and keeps the current volume shape through its
native dismiss animation. Whole-screen tint adjusts mode 106 while preserving
structural/unknown blend modes; zero extra zoom retains its native meaning.
Scopes no longer show an unrelated preview. Launcher art directly identifies the
module's function. See [implementation evidence](design/MATERIAL_PERFORMANCE_015.md).
Device rendering and cold-entry smoothness remain device acceptance.

Version 0.1.4 separates card materials from the whole notification/Control Center
backdrop, including independent values, enable flags, migration and refresh.
The App now has Home, Settings and About pages, compact peer controls, separate
material/corner destinations, and native system/light/dark and Chinese/English
preferences. Notification grouping is suspended. Ordinary hardware-volume
updates now enter the narrow phase, with an accurate two-button preview.
Both maintained Apps share a restrained vector icon family.
See [independent layers](design/MATERIAL_LAYERS.md) and the
current host checks; phone rendering remains acceptance.

Version 0.1.3 isolates whole-background tuning from heads-up banners and retires
closed-panel background snapshots. It requires a real expanded panel in a
confirmed unlocked/non-AOD scene; unknown/locked scenes retain native backgrounds.
See [shade ownership](design/SHADE_BACKGROUND_SCOPE.md) for the confirmed input
path, retained OEM contracts and device acceptance.

Version 0.1.2 adds single-tap scrollable editor tabs and corrects the borrowed
first-show/repeated-key volume behavior. The visible volume track has an explicit
physical-pixel edge gap (6 px by default; 0 is flush). Existing package, signing
identity, preference group and configuration format remain upgrade-compatible.
See [HyperLight behavior review](design/HYPERLIGHT_PARITY.md) for recovered
contracts, corrected differences, retained defaults and device-test boundaries.
The [HyperChanger glass comparison](design/HYPERCHANGER_GLASS_PARITY.md) records
matching core math/recipes and remaining scope/sampling differences. The native
App preview now uses OEM blend colors without a second painted veil.
Earlier preview versions and their archived filenames remain historical records.

The retained scope is native soft-glass tuning, notification materials and
interactions, Control Center corners, volume controls, Personal Assistant widgets
and the SecurityCenter sidebar. Local interactive previews use the same tuning and
motion models. OEM material support is identified separately from a visual fallback.

This fork does not bundle a launcher, music/lyrics engine, iOS notification-center
replacement, camera module, third-party navigation adaptations, OCR, or prebuilt
native patch library. HMC Duo remains the owner of its lockscreen content.
See [deferred ownership](design/DEFERRED.md), [recovery boundaries](design/RECOVERY.md)
and host validation.
The settings App uses [Miuix](https://github.com/compose-miuix-ui/miuix). Material
editing has one entry with a pinned top preview; notification and volume behavior
have separate pages. See the [App design](design/APP.md).

Preview 1 had a plugin-discovery startup regression. Preview 2 contains a guarded,
deferred discovery repair; read the [startup recovery boundary](design/STARTUP_SAFETY.md)
before any device scope trial. Host verification does not establish device recovery.

Preview 3 separates notification music material from ordinary notifications and
adds an ordinary-lockscreen effects range. Fresh installs/reset disable both new
ranges; upgrades preserve their old behavior until explicitly changed. Read
[HMC coexistence](design/HMC_COMPATIBILITY.md) for the supported configurations,
migration and paired acceptance boundary. The modules do not exchange settings
or require a shared service.

Preview 4 adds the six original HyperChanger material recipes under
the material editor's **预设** category, with A/E shortcuts, a material-only trial restore and a
contrast test background. Native-versus-schematic preview status is explicit;
existing bound notifications can refresh on first tuning enable without waiting
for a later OEM update. See [material comparisons](design/MATERIAL_PRESETS.md).


Preview 6 integrates the unified material editor and a usability/behavior review:
clearer task sections and dependency hints, smaller slider windows with one-step
and exact input, thin-volume companion-button handling, accurate disabled-effect
previews, and corrected overdamping. Existing values and scopes are preserved.
See Preview 6 review. The normal-notification sample
is a local preview, not a guarantee of identical rendering for every OEM scene.

Preview 7 simplifies every feature page to one pinned, automatically focused
rendered sample and title-only controls. Notification group lifecycle repair is
included in the same iteration. See Preview 7 review.

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
