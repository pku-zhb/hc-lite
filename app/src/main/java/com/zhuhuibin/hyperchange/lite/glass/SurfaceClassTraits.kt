// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

/** Only immutable class-name predicates are retained; view ownership stays live. */
internal class SurfaceClassTraits {
    data class Flags(val excludedByName: Boolean, val mediaOwner: Boolean, val controlCenterAncestor: Boolean)

    private val classes = object : ClassValue<Flags>() {
        override fun computeValue(type: Class<*>): Flags = fromName(type.name)
    }

    fun get(type: Class<*>): Flags = classes.get(type)

    companion object {
        fun fromName(name: String): Flags = Flags(
            excludedByName = name.startsWith("com.zhuhuibin.") || name.startsWith("btm.m.") ||
                name.contains("dynamicisland", true) || name.contains("mediaisland", true) ||
                name.contains("TimeView") || name.contains("clock", true) ||
                name.contains("MiuiKeyguardClock") || name.contains("NumPadKey"),
            mediaOwner = mediaOwner(name),
            controlCenterAncestor = name.startsWith("miui.systemui.controlcenter.") ||
                name.startsWith("com.miui.systemui.controlcenter."),
        )

        fun mediaOwner(name: String): Boolean =
            name.startsWith("com.android.systemui.statusbar.notification.mediacontrol.") ||
                (name.startsWith("com.android.systemui.") &&
                    (name.contains("mediaheader", true) || name.contains("miuimedia", true) || name.contains("mediarow", true)) &&
                    !name.contains("mediaisland", true) && !name.contains("controlcenter", true))
    }
}
