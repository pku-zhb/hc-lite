#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
volume="${DEV_VOLUME:?Set DEV_VOLUME to a writable directory for build outputs and caches}"
if [[ ! -d "$volume" || ! -w "$volume" ]]; then
    echo 'DEV_VOLUME must be an existing, writable directory.' >&2
    exit 1
fi
cache="${LITE_DEV_CACHE:-$volume/hc-lite}"
# Worktrees can isolate every mutable build output while sharing downloaded tools.
case "$cache" in
    "$volume/"*) ;;
    *) echo 'LITE_DEV_CACHE must be inside DEV_VOLUME.' >&2; exit 1 ;;
esac
# Reuse installed tools, without retaining another source tree.
existing_tools="${DEV_TOOLS_DIR:-$volume/tools}"
export ANDROID_HOME="${ANDROID_HOME:-$existing_tools/android-sdk}"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$existing_tools/gradle}"
if [[ -z "${JAVA_HOME:-}" ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v '21+' 2>/dev/null || true)"
fi
[[ -x "${JAVA_HOME:-}/bin/javac" ]] || { echo 'Set JAVA_HOME to a JDK 21+.' >&2; exit 1; }
[[ -d "$ANDROID_HOME/platforms/android-37" || -d "$ANDROID_HOME/platforms/android-37.0" ]] || { echo 'Set ANDROID_HOME to an SDK with API 37.' >&2; exit 1; }
export LITE_BUILD_ROOT="$cache/build"
export TMPDIR="$cache/tmp"
mkdir -p "$LITE_BUILD_ROOT" "$TMPDIR" "$cache/project-cache" "$cache/kotlin"
if [[ $# == 0 ]]; then set -- :app:testDebugUnitTest :app:assembleDebug; fi
cd "$project_root"
exec ./gradlew --no-daemon --console=plain --max-workers="${LITE_GRADLE_MAX_WORKERS:-4}" \
    --project-cache-dir "$cache/project-cache" -I tools/gradle-init.gradle \
    "-Djava.io.tmpdir=$TMPDIR" "-Pkotlin.project.persistent.dir=$cache/kotlin" "$@"
