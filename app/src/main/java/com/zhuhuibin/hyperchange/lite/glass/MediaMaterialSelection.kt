// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

/** Media unification may adjust a settled awake selection, never replace an AOD recipe. */
internal object MediaMaterialPolicy {
    private val awakeEffects = setOf("MediaViewNormalEffect", "MediaViewBlurEffect", "MediaViewBlurOnKeyguardEffect",
        "MediaViewGlassEffect", "MediaViewGlassOnKeyguardEffect", "MediaViewGlassOnKeyguardLightWallPaperEffect")
    fun mayUnify(awake: Boolean, transitioning: Boolean?, fullAod: Boolean?, effect: String?): Boolean =
        awake && transitioning == false && fullAod == false && effect in awakeEffects
}

/** Nested Blur initialization belongs to the outer OEM effect that selected it. */
internal class MediaMaterialSelection<K : Any, C : Any> {
    internal class Frame<K : Any, C : Any>(val root: K, val effect: String, val context: C,
        val eligible: Boolean, val outermost: Boolean, internal val previous: Frame<K, C>?)
    private val current = ThreadLocal<Frame<K, C>>()

    fun enter(root: K, effect: String, context: C, eligible: () -> Boolean): Frame<K, C> {
        val previous = current.get()
        var owner = previous
        while (owner != null && owner.root !== root) owner = owner.previous
        return Frame(root, owner?.effect ?: effect, owner?.context ?: context,
            owner?.eligible ?: eligible(), owner == null, previous).also(current::set)
    }

    fun selection(root: K): Frame<K, C>? {
        var frame = current.get()
        while (frame != null && frame.root !== root) frame = frame.previous
        return frame
    }

    fun leave(frame: Frame<K, C>) {
        if (frame.previous == null) current.remove() else current.set(frame.previous)
    }
}
