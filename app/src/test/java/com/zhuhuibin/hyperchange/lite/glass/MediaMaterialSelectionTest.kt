// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import org.junit.Assert.*
import org.junit.Test

class MediaMaterialSelectionTest {
    @Test fun fullAodKeepsItsOwnRecipeEvenWhenItsInnerBlurLooksLikeAnAwakeEffect() {
        val selections = MediaMaterialSelection<Any, String>()
        val root = Any()
        val outer = selections.enter(root, "MediaViewGlassFullAodEffect", "aod context") {
            MediaMaterialPolicy.mayUnify(true, false, false, "MediaViewGlassFullAodEffect")
        }
        val blur = selections.enter(root, "MediaViewBlurEffect", "other context") { true }
        assertFalse(blur.outermost)
        assertFalse(blur.eligible)
        assertEquals("MediaViewGlassFullAodEffect", blur.effect)
        assertEquals("aod context", blur.context)
        selections.leave(blur)
        assertSame(outer, selections.selection(root))
        selections.leave(outer)
        assertNull(selections.selection(root))
    }

    @Test fun awakeGlassAndItsNestedBlurUseOneCurrentSelectionAndItsActualContext() {
        val selections = MediaMaterialSelection<Any, String>()
        val root = Any()
        var reads = 0
        val outer = selections.enter(root, "MediaViewGlassOnKeyguardEffect", "native night context") { reads++; true }
        val blur = selections.enter(root, "MediaViewBlurEffect", "stale view context") { reads++; false }
        assertTrue(outer.outermost)
        assertTrue(blur.eligible)
        assertEquals("native night context", blur.context)
        assertEquals("MediaViewGlassOnKeyguardEffect", blur.effect)
        assertEquals(1, reads)
        selections.leave(blur)
        selections.leave(outer)
    }

    @Test fun nativePerFrameWritesOutsideACompleteEffectHaveNoUnifiedInput() {
        val selections = MediaMaterialSelection<Any, String>()
        val root = Any()
        assertNull(selections.selection(root))
        val awake = selections.enter(root, "MediaViewGlassEffect", "current") { true }
        assertTrue(requireNotNull(selections.selection(root)).eligible)
        selections.leave(awake)
        // The next AOD listener frame must not inherit the last awake recipe.
        assertNull(selections.selection(root))
    }

    @Test fun anotherMediaRootCannotBorrowTheFirstCardsRecipeOrEligibility() {
        val selections = MediaMaterialSelection<Any, String>()
        val first = Any()
        val second = Any()
        val awake = selections.enter(first, "MediaViewGlassEffect", "first") { true }
        assertNull(selections.selection(second))
        val aod = selections.enter(second, "MediaViewGlassFullAodEffect", "second") { false }
        assertTrue(aod.outermost)
        assertFalse(requireNotNull(selections.selection(second)).eligible)
        assertSame(awake, selections.selection(first))
        val nestedFirst = selections.enter(first, "MediaViewBlurEffect", "ignored") { error("must reuse owner") }
        assertTrue(nestedFirst.eligible)
        assertEquals("first", nestedFirst.context)
        selections.leave(nestedFirst)
        assertSame(aod, selections.selection(second))
        selections.leave(aod)
        selections.leave(awake)
    }

    @Test fun aThrowingNativeEffectCannotLeakItsSelectionToTheNextTransaction() {
        val selections = MediaMaterialSelection<Any, String>()
        val root = Any()
        val frame = selections.enter(root, "MediaViewGlassEffect", "awake") { true }
        try { throw IllegalStateException("native effect failed") }
        catch (_: IllegalStateException) { }
        finally { selections.leave(frame) }
        assertNull(selections.selection(root))
        val next = selections.enter(root, "MediaViewGlassFullAodEffect", "aod") { false }
        assertFalse(next.eligible)
        selections.leave(next)
    }

    @Test fun onlyAConfirmedSettledAwakeNativeEffectMayUseNormalInputs() {
        for (awake in listOf(false, true)) for (transition in listOf(null, false, true))
            for (aod in listOf(null, false, true)) {
                assertEquals(awake && transition == false && aod == false,
                    MediaMaterialPolicy.mayUnify(awake, transition, aod, "MediaViewGlassOnKeyguardEffect"))
                assertFalse(MediaMaterialPolicy.mayUnify(awake, transition, aod, "MediaViewGlassFullAodEffect"))
            }
        assertFalse(MediaMaterialPolicy.mayUnify(true, false, false, null))
        assertFalse(MediaMaterialPolicy.mayUnify(true, false, false, "FutureMediaEffect"))
    }

    @Test fun unifyOnlyRemainsConfiguredForCurrentSelectorRefreshOutsideAnEffectScope() {
        val permission = NotificationMaterialPolicy(ordinary = false, media = true, tuning = false, unify = true)
        val selections = MediaMaterialSelection<Any, String>()
        assertTrue(permission.force(true, false, false, false))
        assertNull(selections.selection(Any()))
        // No saved normal effect is needed: when the current selector subsequently
        // applies FullAod, normal inputs remain unavailable despite the active config.
        assertFalse(MediaMaterialPolicy.mayUnify(false, false, true, "MediaViewGlassFullAodEffect"))
    }
}
