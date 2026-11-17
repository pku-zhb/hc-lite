// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import org.junit.Assert.*
import org.junit.Test

class SurfaceClassTraitsTest {
    class Fixture
    private class IsolatedLoader : ClassLoader(null) {
        fun define(bytes: ByteArray): Class<*> = defineClass(null, bytes, 0, bytes.size)
    }

    @Test fun positiveAndNegativeClassResultsAreRetained() {
        val traits = SurfaceClassTraits()
        val positive = traits.get(Fixture::class.java)
        val negative = traits.get(String::class.java)
        assertTrue(positive.excludedByName)
        assertEquals(SurfaceClassTraits.Flags(false, false, false), negative)
        repeat(100) {
            assertSame(positive, traits.get(Fixture::class.java))
            assertSame(negative, traits.get(String::class.java))
        }
    }

    @Test fun sameNamedClassesFromDifferentLoadersHaveIndependentEntries() {
        val resource = "/${Fixture::class.java.name.replace('.', '/')}.class"
        val bytes = requireNotNull(Fixture::class.java.getResourceAsStream(resource)).use { it.readBytes() }
        val first = IsolatedLoader().define(bytes)
        val second = IsolatedLoader().define(bytes)
        assertEquals(first.name, second.name)
        assertNotSame(first, second)
        assertNotSame(first.classLoader, second.classLoader)
        val traits = SurfaceClassTraits()
        val firstFlags = traits.get(first)
        val secondFlags = traits.get(second)
        assertEquals(firstFlags, secondFlags)
        assertNotSame(firstFlags, secondFlags)
        assertSame(firstFlags, traits.get(first))
        assertSame(secondFlags, traits.get(second))
    }

    @Test fun mediaNamesPreserveAlternativePrecedenceAndCaseRules() {
        val expected = mapOf(
            "com.android.systemui.statusbar.notification.mediacontrol.MediaIslandControlCenter" to true,
            "com.android.systemui.foo.mEdIaHeAdEr" to true,
            "com.android.systemui.foo.MIuIMeDIAView" to true,
            "com.android.systemui.foo.MediaRow" to true,
            "com.android.systemui.foo.MiuiMediaControlCenter" to false,
            "com.android.systemui.foo.MediaHeaderMediaIsland" to false,
            "com.android.systemuix.foo.MediaHeader" to false,
            "Com.android.systemui.foo.MediaHeader" to false,
            "com.zhuhuibin.hypermusiccover.duo.MiuiMediaOverlay" to false,
        )
        expected.forEach { (name, media) ->
            assertEquals(name, media, SurfaceClassTraits.fromName(name).mediaOwner)
        }
    }

    @Test fun exclusionNamesRetainTheirIndividualCaseRules() {
        val expected = mapOf(
            "x.DynamicIsland" to true,
            "x.MEDIAISLAND" to true,
            "x.CLOCK" to true,
            "x.TimeView" to true,
            "x.timeview" to false,
            "x.NumPadKey" to true,
            "x.NumpadKey" to false,
            "btm.m.Marker" to true,
            "btm.M.Marker" to false,
            "com.zhuhuibin.Marker" to true,
            "com.zhuhuibinX.Marker" to false,
            "android.widget.FrameLayout" to false,
        )
        expected.forEach { (name, excluded) ->
            assertEquals(name, excluded, SurfaceClassTraits.fromName(name).excludedByName)
        }
    }

    @Test fun controlCenterAncestorsRetainBothExactPrefixes() {
        val expected = mapOf(
            "miui.systemui.controlcenter.Card" to true,
            "com.miui.systemui.controlcenter.Card" to true,
            "com.android.systemui.controlcenter.Card" to false,
            "Miui.systemui.controlcenter.Card" to false,
            "com.miui.systemui.ControlCenter.Card" to false,
            "miui.systemui.controlcenterx.Card" to false,
        )
        expected.forEach { (name, controlCenter) ->
            assertEquals(name, controlCenter, SurfaceClassTraits.fromName(name).controlCenterAncestor)
        }
    }
}
