// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import android.view.View
import com.zhuhuibin.hypermusiccover.duo.NativeMaterialTarget
import org.junit.Assert.*
import org.junit.Test

class HmcOwnedMaterialsTest {
    private open class Target(var bits: Int) : View(null), NativeMaterialTarget {
        var version = 1
        var owner: Target? = null
        var other: Target? = null
        var claims = 0
        var reapplies = 0
        var mayClaim = true
        var mayReapply = true
        var afterReapply: (() -> Unit)? = null
        override fun hmcMaterialContractVersion() = version
        override fun hmcMaterialState() = bits
        override fun hmcMaterialOwner(): View? = owner
        override fun hmcMaterialClaim(version: Int): Boolean {
            claims++
            if (!mayClaim || version != 1) return false
            bits = bits or HmcMaterialProtocol.CLAIMED
            other?.let { it.bits = it.bits or HmcMaterialProtocol.CLAIMED }
            return true
        }
        override fun hmcMaterialReapply(): Boolean {
            reapplies++
            afterReapply?.invoke()
            return mayReapply
        }
    }

    private fun pair(): Pair<Target, Target> {
        val glass = Target(HmcMaterialProtocol.GLASS or HmcMaterialProtocol.FOLLOW or HmcMaterialProtocol.KEYGUARD)
        val blur = Target(HmcMaterialProtocol.BLUR or HmcMaterialProtocol.FOLLOW or HmcMaterialProtocol.KEYGUARD)
        glass.owner = glass; blur.owner = glass
        glass.other = blur; blur.other = glass
        return glass to blur
    }

    @Test fun eitherFirstSetterClaimsTheWholePairAndRefreshesItsOwnerOnce() {
        val adapter = HmcOwnedMaterials()
        val (glass, blur) = pair()
        assertSame(glass, adapter.claim(blur)?.owner)
        assertTrue(glass.bits and HmcMaterialProtocol.CLAIMED != 0)
        assertTrue(blur.bits and HmcMaterialProtocol.CLAIMED != 0)
        assertSame(glass, adapter.claim(glass)?.owner)
        assertEquals(1, adapter.refresh())
        assertEquals(1, glass.reapplies)
        assertEquals(0, blur.reapplies)
    }

    @Test fun missingContractAndDuckTypedViewsNeverParticipate() {
        val adapter = HmcOwnedMaterials()
        assertNull(adapter.claim(View(null)))
        assertNull(adapter.claim(object : View(null) {
            @Suppress("unused") fun hmcMaterialState() = 1
            @Suppress("unused") fun hmcMaterialContractVersion() = 1
        }))
        assertEquals(0, adapter.refresh())
    }

    @Test fun oldProtocolUnregisteredAndUnknownRolesFailBeforeClaim() {
        val adapter = HmcOwnedMaterials()
        for (invalid in listOf(0, 3, 1 or 64)) {
            val (glass, _) = pair()
            glass.bits = invalid
            assertNull(adapter.claim(glass))
            assertEquals(0, glass.claims)
        }
        val (old, _) = pair()
        old.version = 0
        assertNull(adapter.claim(old))
        assertEquals(0, old.claims)
        val unregistered = Target(HmcMaterialProtocol.GLASS)
        assertNull(adapter.claim(unregistered))
        assertEquals(0, unregistered.claims)
    }

    @Test fun invalidOwnerRoleIdentityOrClaimCannotGrantAccess() {
        val adapter = HmcOwnedMaterials()
        val (glass, blur) = pair()
        blur.owner = blur
        assertNull(adapter.claim(blur))
        blur.owner = glass
        glass.owner = blur
        assertNull(adapter.claim(blur))
        glass.owner = glass
        glass.version = 2
        assertNull(adapter.claim(blur))
        glass.version = 1
        blur.mayClaim = false
        assertNull(adapter.claim(blur))
        assertEquals(0, adapter.refresh())
    }

    @Test fun settingsAndSceneRefreshAlwaysAskForFreshInputIncludingDisabledTuning() {
        val adapter = HmcOwnedMaterials()
        val (glass, blur) = pair()
        val disabled = HmcMaterialPolicy(false, false, false, true)
        assertNotNull(adapter.claim(blur))
        assertFalse(disabled.tune(glass.bits))
        assertEquals(1, adapter.refresh())
        glass.bits = glass.bits or HmcMaterialProtocol.NATIVE_AOD
        assertEquals(1, adapter.refresh())
        assertEquals(2, glass.reapplies)
        assertTrue(glass.bits and HmcMaterialProtocol.CLAIMED != 0)
    }

    @Test fun reapplySetterDiscoveryCannotCreateARecursiveRefreshOrDuplicateOwner() {
        val adapter = HmcOwnedMaterials()
        val (glass, blur) = pair()
        assertNotNull(adapter.claim(glass))
        glass.afterReapply = {
            assertNotNull(adapter.claim(blur))
            assertEquals(0, adapter.refresh())
        }
        assertEquals(1, adapter.refresh())
        assertEquals(1, glass.reapplies)
    }

    @Test fun aFailedCallbackRetriesAndAnInvalidatedPairIsForgotten() {
        val adapter = HmcOwnedMaterials()
        val (glass, _) = pair()
        assertNotNull(adapter.claim(glass))
        glass.mayReapply = false
        assertEquals(0, adapter.refresh())
        glass.mayReapply = true
        assertEquals(1, adapter.refresh())
        glass.owner = null
        assertEquals(0, adapter.refresh())
        glass.owner = glass
        assertEquals(0, adapter.refresh())
        assertEquals(2, glass.reapplies)
    }

    @Test fun positiveAndNegativeResolutionAreCachedByClassIdentity() {
        var resolutions = 0
        val accessors = HmcMaterialAccessors { type ->
            resolutions++
            if (NativeMaterialTarget::class.java.isAssignableFrom(type)) NativeMaterialTarget::class.java else null
        }
        repeat(3) { assertNotNull(accessors.get(Target::class.java)); assertNull(accessors.get(View::class.java)) }
        assertEquals(2, resolutions)
    }
}
