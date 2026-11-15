// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import android.content.SharedPreferences
import android.graphics.Point
import android.view.View
import com.zhuhuibin.hyperchange.lite.HookRuntime
import com.zhuhuibin.hypermusiccover.duo.NativeMaterialTarget
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Test
import z71

/** Executes the same dispatcher and arithmetic that production View hooks call. */
class HmcOwnedSetterExecutionTest {
    private class Target(var state: Int) : View(null), NativeMaterialTarget {
        var owner: Target? = null
        var other: Target? = null
        var claims = 0
        var onReapply: (() -> Unit)? = null
        override fun hmcMaterialContractVersion() = 1
        override fun hmcMaterialState() = state
        override fun hmcMaterialOwner(): View? = owner
        override fun hmcMaterialClaim(version: Int): Boolean {
            claims++
            if (version != 1 || owner == null || HmcMaterialProtocol.role(state) == null) return false
            state = state or HmcMaterialProtocol.CLAIMED
            other?.let { it.state = it.state or HmcMaterialProtocol.CLAIMED }
            return true
        }
        override fun hmcMaterialReapply(): Boolean { onReapply?.invoke(); return true }
    }

    private class Harness {
        val adapter = HmcOwnedMaterials()
        val glass = Target(HmcMaterialProtocol.GLASS or HmcMaterialProtocol.FOLLOW or HmcMaterialProtocol.KEYGUARD)
        val blur = Target(HmcMaterialProtocol.BLUR or HmcMaterialProtocol.FOLLOW or HmcMaterialProtocol.KEYGUARD)
        var permission = HmcMaterialPolicy(true, true, true, true)
        var settings = SoftGlassSettings(enabled = true, backgroundOpacity = 50,
            backgroundBlur = 150, backgroundScale = 50, refractionBlur = 50,
            adjustments = mapOf("luminance" to 50))
        var rawGlass = recipe(.4f)
        val normalGlass = recipe(.8f)
        var rawPalette = arrayListOf(Point(0x80445566.toInt(), 101), Point(0x60446677, 102), Point(0x40556677, 103))
        val normalPalette = arrayListOf(Point(0x80667788.toInt(), 201), Point(0x40aabbcc, 202))
        var recipeSelections = 0
        var paletteSelections = 0
        var frameGlass: FloatArray? = null
        val framePalette = arrayListOf<Point>()
        val writes = mutableListOf<String>()
        private val preferences = Proxy.newProxyInstance(SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)) { _, method, _ ->
            if (method.name == "getAll") emptyMap<String, Any>() else null
        } as SharedPreferences
        private val runtime = HookRuntime(object : XposedModule() {}, requireNotNull(javaClass.classLoader), preferences, "com.android.systemui")
        val engine = ViewMaterialEngine(runtime, { error("Owned writes must not use native surface admission") },
            { error("Owned writes must not use native media settings") },
            scopeAllows = { false }, // Permission-off still has to claim and write fresh raw input.
            hmcOwnedMaterials = adapter,
            hmcSettingsFor = { bits -> settings.copy(enabled = permission.tune(bits)) },
            hmcUnifiedRecipe = { _, _ -> recipeSelections++; if (permission.unify) normalGlass else null },
            hmcUnifiedBlends = { _, _ -> paletteSelections++; if (permission.unify) normalPalette else null })

        init {
            glass.owner = glass; glass.other = blur
            blur.owner = glass; blur.other = glass
            glass.onReapply = { applyGlass(); applyBulk() }
        }

        fun write(view: View, name: String, args: Array<Any?>, native: (Array<Any?>) -> Any?): Any? =
            requireNotNull(engine.dispatchHmcOwnedSetter(view, name, { args }, native)).result

        fun applyGlass() = write(glass, "setMiGlass", arrayOf(rawGlass)) {
            frameGlass = (it[0] as? FloatArray)?.copyOf(); Unit
        }

        fun clear() = write(glass, "clearMiBackgroundBlendColor", emptyArray()) {
            writes += "clear"; framePalette.clear(); Unit
        }

        fun add(color: Int, mode: Int) = write(glass, "addMiBackgroundBlendColor", arrayOf(color, mode)) {
            writes += "add"; framePalette.add(Point(it[0] as Int, it[1] as Int)); Unit
        }

        /** The native bulk setter may itself dispatch the ROM's legacy clear/add loop. */
        fun applyBulk() = write(glass, "setMiBackgroundBlendColors", arrayOf(rawPalette)) { args ->
            @Suppress("UNCHECKED_CAST") val selected = args[0] as ArrayList<Point>
            writes += "bulk:${selected.size}"
            clear()
            selected.forEach { add(it.x, it.y) }
            Unit
        }

        fun legacyFrame() { clear(); rawPalette.forEach { add(it.x, it.y) } }

        fun assertNoHistory() {
            for (name in listOf("baselines", "arrayPairs", "legacyBlends", "owned", "scopeTokens")) {
                val map = ViewMaterialEngine::class.java.getDeclaredField(name).apply { isAccessible = true }.get(engine) as Map<*, *>
                assertTrue("Owned setter retained $name", map.isEmpty())
            }
        }
    }

    @Test fun completeAwakeBulkPaletteAndGlassUnifyBeforeAnyLegacyWriteAndTuneOnlyOnce() {
        val h = Harness()
        h.applyGlass(); h.applyBulk()
        assertEquals(1.2f, h.frameGlass!![4], .00001f)
        assertEquals(.4f, h.rawGlass[4], 0f)
        assertEquals(listOf(Point(0x40667788, 201), Point(0x20aabbcc, 202)), h.framePalette)
        assertEquals(listOf("bulk:2", "clear", "add", "add"), h.writes)
        assertEquals(1, h.paletteSelections)
        assertEquals(1, h.recipeSelections)
        assertEquals(3, h.rawPalette.size)
        assertEquals(0x80667788.toInt(), h.normalPalette[0].x)
        h.assertNoHistory()
    }

    @Test fun firstDisabledSetterClaimsAndWritesCurrentRawScalarsAndCompletePalette() {
        val h = Harness()
        h.permission = h.permission.copy(glass = false)
        h.applyGlass(); h.applyBulk()
        assertTrue(h.glass.state and HmcMaterialProtocol.CLAIMED != 0)
        assertTrue(h.blur.state and HmcMaterialProtocol.CLAIMED != 0)
        assertArrayEquals(h.rawGlass, h.frameGlass, 0f)
        assertEquals(h.rawPalette, h.framePalette)
        assertEquals(0, h.paletteSelections)
        assertEquals(0, h.recipeSelections)
        assertEquals(80, h.write(h.blur, "setMiBackgroundBlurRadius", arrayOf(80)) { it[0] })
        assertEquals(.2f, h.write(h.blur, "setMiBackgroundBlurScaleRatio", arrayOf(.2f)) { it[0] })
        assertArrayEquals(arrayOf<Any?>(12, 24), h.write(h.glass, "setMiGlassBlurRadius", arrayOf(12, 24)) { it } as Array<*>)
        h.assertNoHistory()
    }

    @Test fun liveScalarsUseRawInputOnceAndFollowOrKeyguardOffReturnsThemUnchanged() {
        val h = Harness()
        assertEquals(120, h.write(h.blur, "setMiBackgroundBlurRadius", arrayOf(80)) { it[0] })
        assertEquals(.1f, h.write(h.blur, "setMiBackgroundBlurScaleRatio", arrayOf(.2f)) { it[0] })
        assertArrayEquals(arrayOf<Any?>(6, 12), h.write(h.glass, "setMiGlassBlurRadius", arrayOf(12, 24)) { it } as Array<*>)
        h.blur.state = h.blur.state xor HmcMaterialProtocol.FOLLOW
        assertEquals(80, h.write(h.blur, "setMiBackgroundBlurRadius", arrayOf(80)) { it[0] })
        h.glass.state = h.glass.state xor HmcMaterialProtocol.KEYGUARD
        h.applyGlass(); h.applyBulk()
        assertArrayEquals(h.rawGlass, h.frameGlass, 0f)
        assertEquals(h.rawPalette, h.framePalette)
        h.assertNoHistory()
    }

    @Test fun disableRefreshRebuildsChangedRawRecipeInsteadOfReplayingThePreviousInput() {
        val h = Harness()
        h.applyGlass(); h.applyBulk()
        h.permission = h.permission.copy(notifications = false)
        h.rawGlass = recipe(.9f)
        h.rawPalette = arrayListOf(Point(0x90554433.toInt(), 301))
        assertEquals(1, h.adapter.refresh())
        assertEquals(.9f, h.frameGlass!![4], 0f)
        assertEquals(h.rawPalette, h.framePalette)
        assertEquals(listOf("bulk:2", "clear", "add", "add", "bulk:1", "clear", "add"), h.writes)
        assertTrue(h.glass.state and HmcMaterialProtocol.CLAIMED != 0)
        h.assertNoHistory()
    }

    @Test fun nativeAodBulkKeepsItsCompleteThreeColorFrameAndNeverSelectsAwakeResources() {
        val h = Harness()
        h.glass.state = h.glass.state or HmcMaterialProtocol.NATIVE_AOD
        h.applyGlass(); h.applyBulk()
        assertEquals(.6f, h.frameGlass!![4], .00001f)
        assertEquals(listOf(Point(0x40445566, 101), Point(0x30446677, 102), Point(0x20556677, 103)), h.framePalette)
        assertEquals(listOf("bulk:3", "clear", "add", "add", "add"), h.writes)
        assertEquals(0, h.paletteSelections)
        assertEquals(0, h.recipeSelections)
        h.assertNoHistory()
    }

    @Test fun perFrameNativeAodLegacyPaletteKeepsAllModesAndTunesEachColorOnce() {
        val h = Harness()
        h.glass.state = h.glass.state or HmcMaterialProtocol.NATIVE_AOD
        h.legacyFrame()
        assertEquals(listOf(Point(0x40445566, 101), Point(0x30446677, 102), Point(0x20556677, 103)), h.framePalette)
        assertEquals(listOf("clear", "add", "add", "add"), h.writes)
        assertEquals(0, h.paletteSelections)
        h.permission = h.permission.copy(lockscreen = false)
        h.legacyFrame()
        assertEquals(h.rawPalette, h.framePalette)
        h.assertNoHistory()
    }

    @Test fun nullAndZeroGlassRemainNativeClearsWithoutSelectingAnAwakeRecipe() {
        val h = Harness()
        assertNull(h.write(h.glass, "setMiGlass", arrayOf(null)) { it[0] })
        val zero = FloatArray(42)
        assertSame(zero, h.write(h.glass, "setMiGlass", arrayOf(zero)) { it[0] })
        assertEquals(0, h.recipeSelections)
        h.assertNoHistory()
    }

    @Test fun nullAndEmptyBulkPalettesRemainCompleteNativeClears() {
        val h = Harness()
        assertNull(h.write(h.glass, "setMiBackgroundBlendColors", arrayOf(null)) { it[0] })
        val empty = arrayListOf<Point>()
        assertSame(empty, h.write(h.glass, "setMiBackgroundBlendColors", arrayOf(empty)) { it[0] })
        assertEquals(0, h.paletteSelections)
        h.assertNoHistory()
    }

    @Test fun failedBulkRestoresLegacyDispatchForTheNextFrame() {
        val h = Harness()
        assertThrows(IllegalStateException::class.java) {
            h.write(h.glass, "setMiBackgroundBlendColors", arrayOf(h.rawPalette)) { throw IllegalStateException("native commit failed") }
        }
        h.add(0x80445566.toInt(), 101)
        assertEquals(listOf(Point(0x40445566, 101)), h.framePalette)
        h.assertNoHistory()
    }

    @Test fun anUnregisteredRendererProbeRemainsExcludedInsideAnActiveMediaSelection() {
        val h = Harness()
        val probe = z71()
        val media = MediaMaterialSelection<View, Any>()
        val frame = media.enter(probe, "MediaViewGlassEffect", Any()) { true }
        try {
            GlassSurfaces.markMediaRoot(probe)
            assertTrue(GlassSurfaces.excluded(probe))
            assertFalse(GlassSurfaces.media(probe))
            assertNull(GlassSurfaces.classify(probe))
            assertNull(h.engine.dispatchHmcOwnedSetter(probe, "setMiGlass", { error("Unregistered inputs must not be read") }) {
                error("Unregistered probe must not be written")
            })
            assertEquals(0, probe.claims)
            assertEquals(0, h.adapter.refresh())
        } finally { media.leave(frame) }
    }

    companion object {
        private fun recipe(luminance: Float) = FloatArray(42) { .1f }.apply { this[4] = luminance }
    }
}
