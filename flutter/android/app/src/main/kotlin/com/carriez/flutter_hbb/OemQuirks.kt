package com.carriez.flutter_hbb

import android.os.Build

/**
 * Phase 81-04 — per-OEM / per-Android-version tuning for the unattended auto-accept of the
 * MediaProjection ("Share your screen?") consent dialog.
 *
 * The dialog's layout, the package that hosts it, and whether its nodes are readable all vary by
 * OEM skin and Android version (81-00 findings). Coordinates here are PROPORTIONAL (fractions of
 * the screen width/height) so a single profile scales across resolutions of the same layout —
 * only the per-OEM LAYOUT differences need separate profiles.
 *
 * VERIFIED profiles: Samsung One UI / Android 14+ (proven on S22 Ultra 1080x2316 / Android 16,
 * spike 81-02 + 81-02 live). Pixel/Motorola/OnePlus use the GENERIC profile (node-click first; the
 * coordinate fallback is best-effort and MUST be validated on a physical unit before being trusted
 * — 81-04 Task 3). Xiaomi/MIUI is intentionally absent (D-05, out of v1).
 */
data class AutoAcceptProfile(
    /** System-UI package(s) that host the consent dialog. */
    val dialogPkgs: Set<String>,
    /** Any of these texts present ⇒ this is the projection dialog. */
    val markers: List<String>,
    /** "Entire screen" dropdown option label variants (node-click path). */
    val entireScreenTexts: List<String>,
    /** Confirm-button label variants (node-click path). */
    val confirmTexts: List<String>,
    /** Proportional tap target for the source dropdown ("Share one app" ▾). */
    val dropdown: Pair<Float, Float>,
    /** Proportional tap target for the "Entire screen" item once the dropdown is open. */
    val entire: Pair<Float, Float>,
    /** Proportional tap target for the final "Share/Start" button. */
    val share: Pair<Float, Float>,
    /** When true, fall back to coordinate gestures if node-click fails (secure overlay). */
    val useCoordinateFallback: Boolean,
)

object OemQuirks {

    // Samsung One UI, Android 14+ (SDK 34+). Dialog is a secure overlay → nodes unreadable →
    // coordinate path is PRIMARY. Calibrated on S22 Ultra 1080x2316 / One UI 8 / Android 16.
    private val SAMSUNG_A14PLUS = AutoAcceptProfile(
        dialogPkgs = setOf("com.android.systemui", "android"),
        markers = listOf(
            "Share your screen", "Start now", "Start recording", "Entire screen",
            "Share entire screen", "Share screen", "cast", "record"
        ),
        entireScreenTexts = listOf("Entire screen", "Share entire screen"),
        confirmTexts = listOf("Share screen", "Start now", "Start recording", "Start", "Allow"),
        dropdown = 0.500f to 0.460f,
        entire = 0.370f to 0.550f,
        share = 0.787f to 0.684f,
        useCoordinateFallback = true,
    )

    // Generic AOSP / Pixel / Motorola / OnePlus. Node-click usually works on these; the coordinate
    // values mirror the Samsung layout as a best-effort fallback and are UNVERIFIED per brand
    // (81-04 Task 3 calibrates each on a physical unit).
    private val GENERIC = AutoAcceptProfile(
        dialogPkgs = setOf("com.android.systemui", "android"),
        markers = listOf(
            "Share your screen", "Start now", "Start recording", "Entire screen",
            "Share entire screen", "Share screen", "cast", "record"
        ),
        entireScreenTexts = listOf("Entire screen", "Share entire screen"),
        confirmTexts = listOf("Start now", "Allow", "Share screen", "Start", "Start recording"),
        dropdown = 0.500f to 0.460f,
        entire = 0.370f to 0.550f,
        share = 0.787f to 0.684f,
        useCoordinateFallback = true,
    )

    /** Selects the auto-accept profile for THIS device. */
    fun current(): AutoAcceptProfile {
        val m = (Build.MANUFACTURER ?: "").lowercase()
        return when {
            m.contains("samsung") && Build.VERSION.SDK_INT >= 34 -> SAMSUNG_A14PLUS
            else -> GENERIC
        }
    }
}
