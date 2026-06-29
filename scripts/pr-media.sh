#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

PLATFORM="${1:-all}"
MEDIA_TYPE="${2:-all}"

if [ "$PLATFORM" != "android" ] && [ "$PLATFORM" != "ios" ] && [ "$PLATFORM" != "all" ]; then
    MEDIA_TYPE="$PLATFORM"
    PLATFORM="all"
fi

PACKAGE_NAME="${PR_MEDIA_PACKAGE_NAME:-com.peto.ramap}"
ACTIVITY_NAME="${PR_MEDIA_ACTIVITY_NAME:-.MainActivity}"
IOS_BUNDLE_ID="${PR_MEDIA_IOS_BUNDLE_ID:-com.peto.ramap.Ramap}"
IOS_WORKSPACE="${PR_MEDIA_IOS_WORKSPACE:-iosApp/iosApp.xcworkspace}"
IOS_SCHEME="${PR_MEDIA_IOS_SCHEME:-iosApp}"
IOS_CONFIGURATION="${PR_MEDIA_IOS_CONFIGURATION:-Debug}"
IOS_APP_NAME="${PR_MEDIA_IOS_APP_NAME:-Ramap}"
IOS_SIMULATOR_NAME="${PR_MEDIA_IOS_SIMULATOR_NAME:-iPhone 17}"
RECORD_SECONDS="${PR_MEDIA_RECORD_SECONDS:-8}"
LAUNCH_WAIT_SECONDS="${PR_MEDIA_LAUNCH_WAIT_SECONDS:-5}"
IOS_INSTALL_TIMEOUT_SECONDS="${PR_MEDIA_IOS_INSTALL_TIMEOUT_SECONDS:-60}"
IOS_TERMINATE_TIMEOUT_SECONDS="${PR_MEDIA_IOS_TERMINATE_TIMEOUT_SECONDS:-10}"
IOS_LAUNCH_TIMEOUT_SECONDS="${PR_MEDIA_IOS_LAUNCH_TIMEOUT_SECONDS:-20}"
IOS_CAPTURE_TIMEOUT_SECONDS="${PR_MEDIA_IOS_CAPTURE_TIMEOUT_SECONDS:-20}"
REMOTE_RECORDING_PATH="${PR_MEDIA_REMOTE_RECORDING_PATH:-/sdcard/ramap-pr-recording.mp4}"
BRANCH_NAME="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo local)"
SAFE_BRANCH_NAME="$(printf '%s' "$BRANCH_NAME" | tr '/: ' '---')"
OUTPUT_DIR="${PR_MEDIA_OUTPUT_DIR:-build/pr-media/$SAFE_BRANCH_NAME}"
SUMMARY_PATH="$OUTPUT_DIR/pr-media.md"
APK_PATH="${PR_MEDIA_APK_PATH:-$ROOT_DIR/androidApp/build/outputs/apk/debug/androidApp-debug.apk}"
ANDROID_SCREENSHOT_PATH="$OUTPUT_DIR/android-screenshot.png"
ANDROID_RECORDING_PATH="$OUTPUT_DIR/android-recording.mp4"
IOS_SCREENSHOT_PATH="$OUTPUT_DIR/ios-screenshot.png"
IOS_RECORDING_PATH="$OUTPUT_DIR/ios-recording.mp4"

ANDROID_SCREENSHOT_CELL="-"
ANDROID_RECORDING_CELL="-"
IOS_SCREENSHOT_CELL="-"
IOS_RECORDING_CELL="-"

fail() {
    echo "error: $*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || fail "'$1' command is required."
}

run_with_timeout() {
    local seconds="$1"
    shift

    "$@" &
    local command_pid="$!"

    (
        sleep "$seconds"
        kill -TERM "$command_pid" >/dev/null 2>&1 || true
    ) &
    local timer_pid="$!"

    local result=0
    wait "$command_pid" 2>/dev/null || result="$?"
    kill "$timer_pid" >/dev/null 2>&1 || true
    wait "$timer_pid" >/dev/null 2>&1 || true

    return "$result"
}

should_run_platform() {
    [ "$PLATFORM" = "$1" ] || [ "$PLATFORM" = "all" ]
}

should_capture() {
    [ "$MEDIA_TYPE" = "$1" ] || [ "$MEDIA_TYPE" = "all" ]
}

adb_device_count() {
    adb devices | awk 'NR > 1 && $2 == "device" { count++ } END { print count + 0 }'
}

booted_ios_device() {
    xcrun simctl list devices |
        sed -n 's/.*(\([0-9A-Fa-f-]\{36\}\)) (Booted).*/\1/p' |
        head -n 1
}

ios_app_path() {
    if [ -n "${PR_MEDIA_IOS_APP_PATH:-}" ]; then
        printf '%s\n' "$PR_MEDIA_IOS_APP_PATH"
        return
    fi

    find "$HOME/Library/Developer/Xcode/DerivedData" \
        -path "*/Build/Products/${IOS_CONFIGURATION}-iphonesimulator/${IOS_APP_NAME}.app" \
        -not -path "*/Index.noindex/*" \
        -type d \
        -print |
        sort |
        tail -n 1
}

render_summary() {
    cat > "$SUMMARY_PATH" <<EOF
## PR Media

| Type | AN | IOS |
| --- | --- | --- |
EOF

    if should_capture screenshot; then
        cat >> "$SUMMARY_PATH" <<EOF
| Screenshot | $ANDROID_SCREENSHOT_CELL | $IOS_SCREENSHOT_CELL |
EOF
    fi

    if should_capture recording; then
        cat >> "$SUMMARY_PATH" <<EOF
| Recording | $ANDROID_RECORDING_CELL | $IOS_RECORDING_CELL |
EOF
    fi
}

run_android_media() {
    require_command adb

    local device_count
    device_count="$(adb_device_count)"

    if [ "$device_count" -eq 0 ]; then
        fail "No Android device is available. Start an emulator or connect a device, then run this script again."
    fi

    if [ "$device_count" -gt 1 ] && [ -z "${ANDROID_SERIAL:-}" ]; then
        fail "Multiple Android devices are available. Set ANDROID_SERIAL to choose one."
    fi

    if [ "${PR_MEDIA_SKIP_ANDROID_BUILD:-${PR_MEDIA_SKIP_BUILD:-false}}" != "true" ]; then
        ./gradlew :androidApp:assembleDebug
    fi

    [ -f "$APK_PATH" ] || fail "Debug APK was not found at $APK_PATH."

    adb install -r "$APK_PATH" >/dev/null
    adb shell am force-stop "$PACKAGE_NAME" >/dev/null 2>&1 || true
    adb shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME" >/dev/null
    sleep "$LAUNCH_WAIT_SECONDS"

    if should_capture screenshot; then
        adb exec-out screencap -p > "$ANDROID_SCREENSHOT_PATH"
        ANDROID_SCREENSHOT_CELL="<img src=\"android-screenshot.png\" width=\"210\" height=\"450\">"
        echo "- $ANDROID_SCREENSHOT_PATH"
    fi

    if should_capture recording; then
        adb shell rm -f "$REMOTE_RECORDING_PATH" >/dev/null 2>&1 || true
        adb shell screenrecord --time-limit "$RECORD_SECONDS" "$REMOTE_RECORDING_PATH"
        adb pull "$REMOTE_RECORDING_PATH" "$ANDROID_RECORDING_PATH" >/dev/null
        adb shell rm -f "$REMOTE_RECORDING_PATH" >/dev/null 2>&1 || true
        ANDROID_RECORDING_CELL="[Android recording](android-recording.mp4)"
        echo "- $ANDROID_RECORDING_PATH"
    fi
}

run_ios_media() {
    require_command xcrun
    require_command xcodebuild

    if [ "${PR_MEDIA_SKIP_IOS_BUILD:-${PR_MEDIA_SKIP_BUILD:-false}}" != "true" ]; then
        xcodebuild \
            -workspace "$IOS_WORKSPACE" \
            -scheme "$IOS_SCHEME" \
            -sdk iphonesimulator \
            -configuration "$IOS_CONFIGURATION" \
            build
    fi

    local device
    device="${PR_MEDIA_IOS_DEVICE:-$(booted_ios_device)}"

    if [ -z "$device" ]; then
        device="$IOS_SIMULATOR_NAME"
        xcrun simctl boot "$device" >/dev/null 2>&1 || true
        xcrun simctl bootstatus "$device" -b >/dev/null
        device="$(booted_ios_device)"
    fi

    [ -n "$device" ] || fail "No iOS simulator is available. Boot a simulator or set PR_MEDIA_IOS_DEVICE."

    local app_path
    app_path="$(ios_app_path)"
    [ -n "$app_path" ] && [ -d "$app_path" ] || fail "iOS app was not found. Set PR_MEDIA_IOS_APP_PATH or build the iOS app first."

    if [ "${PR_MEDIA_SKIP_IOS_INSTALL:-false}" != "true" ]; then
        run_with_timeout "$IOS_INSTALL_TIMEOUT_SECONDS" xcrun simctl install "$device" "$app_path" >/dev/null ||
            fail "Timed out while installing the iOS app on the simulator."
    fi

    run_with_timeout "$IOS_TERMINATE_TIMEOUT_SECONDS" xcrun simctl terminate "$device" "$IOS_BUNDLE_ID" >/dev/null 2>&1 || true
    run_with_timeout "$IOS_LAUNCH_TIMEOUT_SECONDS" xcrun simctl launch "$device" "$IOS_BUNDLE_ID" >/dev/null ||
        fail "Timed out while launching the iOS app on the simulator."
    sleep "$LAUNCH_WAIT_SECONDS"

    if should_capture screenshot; then
        run_with_timeout "$IOS_CAPTURE_TIMEOUT_SECONDS" xcrun simctl io "$device" screenshot "$IOS_SCREENSHOT_PATH" >/dev/null ||
            fail "Timed out while capturing the iOS screenshot."
        IOS_SCREENSHOT_CELL="<img src=\"ios-screenshot.png\" width=\"210\" height=\"450\">"
        echo "- $IOS_SCREENSHOT_PATH"
    fi

    if should_capture recording; then
        rm -f "$IOS_RECORDING_PATH"
        xcrun simctl io "$device" recordVideo "$IOS_RECORDING_PATH" >/dev/null &
        local record_pid="$!"
        sleep "$RECORD_SECONDS"
        kill -INT "$record_pid" >/dev/null 2>&1 || true
        wait "$record_pid" >/dev/null 2>&1 || true
        IOS_RECORDING_CELL="[iOS recording](ios-recording.mp4)"
        echo "- $IOS_RECORDING_PATH"
    fi
}

case "$PLATFORM" in
    android | ios | all) ;;
    *) fail "Unsupported platform '$PLATFORM'. Use one of: android, ios, all." ;;
esac

case "$MEDIA_TYPE" in
    screenshot | recording | all) ;;
    *) fail "Unsupported media type '$MEDIA_TYPE'. Use one of: screenshot, recording, all." ;;
esac

mkdir -p "$OUTPUT_DIR"

echo "PR media generated:"

if should_run_platform android; then
    run_android_media
fi

if should_run_platform ios; then
    run_ios_media
fi

render_summary
echo "- $SUMMARY_PATH"
