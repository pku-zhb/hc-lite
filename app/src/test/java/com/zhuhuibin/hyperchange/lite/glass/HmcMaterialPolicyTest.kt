// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import org.junit.Assert.*
import org.junit.Test

class HmcMaterialPolicyTest {
    private val glass = HmcMaterialProtocol.GLASS or HmcMaterialProtocol.FOLLOW or HmcMaterialProtocol.KEYGUARD
    private val all = HmcMaterialPolicy(true, true, true, true)

    @Test fun materialTuningNeedsEveryExistingPermissionAndTheCurrentHmcGates() {
        assertTrue(all.tune(glass))
        assertTrue(all.tune((glass xor HmcMaterialProtocol.GLASS) or HmcMaterialProtocol.BLUR))
        assertFalse(all.copy(glass = false).tune(glass))
        assertFalse(all.copy(notifications = false).tune(glass))
        assertFalse(all.copy(lockscreen = false).tune(glass))
        assertFalse(all.tune(glass xor HmcMaterialProtocol.FOLLOW))
        assertFalse(all.tune(glass xor HmcMaterialProtocol.KEYGUARD))
        assertFalse(all.tune(glass or HmcMaterialProtocol.BLUR))
        assertFalse(all.tune(glass or 64))
    }

    @Test fun nativeAodKeepsItsOwnFrameWhileUsingTheSameMaterialPermission() {
        assertTrue(all.unify(glass))
        assertFalse(all.copy(unify = false).unify(glass))
        val aod = glass or HmcMaterialProtocol.NATIVE_AOD
        assertTrue(all.tune(aod))
        assertFalse(all.unify(aod))
        assertFalse(all.copy(glass = false).unify(glass))
    }

    @Test fun ownershipClaimDoesNotChangePermissionOrRequireMediaLifecycle() {
        assertEquals(all.tune(glass), all.tune(glass or HmcMaterialProtocol.CLAIMED))
        assertEquals(all.unify(glass), all.unify(glass or HmcMaterialProtocol.CLAIMED))
        // There is deliberately no player-active/media-enabled argument to this policy.
        assertTrue(all.tune(glass))
    }
}
