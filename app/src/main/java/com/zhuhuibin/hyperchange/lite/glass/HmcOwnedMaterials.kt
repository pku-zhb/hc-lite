// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import android.view.View
import java.lang.reflect.Method
import java.util.WeakHashMap

/** Optional v1 protocol. Looking at an interface never grants a module-wide exception. */
internal object HmcMaterialProtocol {
    const val INTERFACE = "com.zhuhuibin.hypermusiccover.duo.NativeMaterialTarget"
    const val VERSION = 1
    const val GLASS = 1
    const val BLUR = 2
    const val FOLLOW = 4
    const val KEYGUARD = 8
    const val NATIVE_AOD = 16
    const val CLAIMED = 32
    private const val KNOWN = GLASS or BLUR or FOLLOW or KEYGUARD or NATIVE_AOD or CLAIMED

    fun role(state: Int): Int? = (state and (GLASS or BLUR)).takeIf {
        state and KNOWN.inv() == 0 && (it == GLASS || it == BLUR)
    }
}

/** This is the ordinary lockscreen notification material range, independent of media. */
internal data class HmcMaterialPolicy(val glass: Boolean, val notifications: Boolean,
                                     val lockscreen: Boolean, val unify: Boolean) {
    fun tune(state: Int): Boolean = HmcMaterialProtocol.role(state) != null &&
        glass && notifications && lockscreen &&
        state and HmcMaterialProtocol.FOLLOW != 0 && state and HmcMaterialProtocol.KEYGUARD != 0

    fun unify(state: Int): Boolean = tune(state) && unify && state and HmcMaterialProtocol.NATIVE_AOD == 0
}

/** Membership excludes generic fallback even for an old/empty ABI or an unregistered probe. */
internal object HmcMaterialMembership {
    private class Match(val contract: Class<*>?)
    private val classes = object : ClassValue<Match>() {
        override fun computeValue(type: Class<*>): Match = Match(hmcMaterialInterface(type))
    }
    fun contract(type: Class<*>): Class<*>? = classes.get(type).contract
    fun contains(type: Class<*>): Boolean = contract(type) != null
}

/** Accessors are attached to their defining class/loader; missing contracts are cached too. */
internal class HmcMaterialAccessors(
    private val find: (Class<*>) -> Class<*>? = HmcMaterialMembership::contract,
) {
    internal class Access(val version: Method, val state: Method, val owner: Method,
                          val claim: Method, val reapply: Method)
    private class Resolution(val access: Access?)
    private val classes = object : ClassValue<Resolution>() {
        override fun computeValue(type: Class<*>): Resolution = Resolution(runCatching {
            val contract = find(type)?.takeIf { it.isInterface && it.name == HmcMaterialProtocol.INTERFACE }
                ?: return@runCatching null
            fun method(name: String, result: Class<*>, vararg arguments: Class<*>): Method =
                contract.getMethod(name, *arguments).also { check(it.returnType == result) }
            Access(method("hmcMaterialContractVersion", Integer.TYPE), method("hmcMaterialState", Integer.TYPE),
                method("hmcMaterialOwner", View::class.java),
                method("hmcMaterialClaim", java.lang.Boolean.TYPE, Integer.TYPE),
                method("hmcMaterialReapply", java.lang.Boolean.TYPE))
        }.getOrNull())
    }
    fun get(type: Class<*>): Access? = classes.get(type).access
}

private fun hmcMaterialInterface(type: Class<*>): Class<*>? {
    if (type.isInterface && type.name == HmcMaterialProtocol.INTERFACE) return type
    type.interfaces.forEach { hmcMaterialInterface(it)?.let { contract -> return contract } }
    return type.superclass?.let(::hmcMaterialInterface)
}

/** Values hold no View: retaining an owner never keeps its view tree or loader alive. */
internal class HmcOwnedMaterials(private val accessors: HmcMaterialAccessors = HmcMaterialAccessors()) {
    internal data class Target(val owner: View, val state: Int)
    private val owners = WeakHashMap<View, Boolean>()
    private val refreshing = ThreadLocal<Boolean>()

    /** Only native setters claim. Querying state and policy has no ownership side effects. */
    fun claim(view: View): Target? = runCatching {
        val target = inspect(view) ?: return@runCatching null
        val access = accessors.get(view.javaClass) ?: return@runCatching null
        if (access.claim.invoke(view, HmcMaterialProtocol.VERSION) != true) return@runCatching null
        val claimed = inspect(view)?.takeIf {
            it.owner === target.owner && it.state and HmcMaterialProtocol.CLAIMED != 0
        } ?: return@runCatching null
        val owner = inspect(claimed.owner)?.takeIf { it.state and HmcMaterialProtocol.CLAIMED != 0 }
            ?: return@runCatching null
        if (owner.owner !== claimed.owner) return@runCatching null
        synchronized(owners) { owners[claimed.owner] = true }
        claimed
    }.getOrNull()

    private fun inspect(view: View): Target? = runCatching {
        val access = accessors.get(view.javaClass) ?: return@runCatching null
        if (access.version.invoke(view) != HmcMaterialProtocol.VERSION) return@runCatching null
        val state = access.state.invoke(view) as? Int ?: return@runCatching null
        val role = HmcMaterialProtocol.role(state) ?: return@runCatching null
        val owner = access.owner.invoke(view) as? View ?: return@runCatching null
        if ((role == HmcMaterialProtocol.GLASS) != (owner === view)) return@runCatching null
        val ownerAccess = accessors.get(owner.javaClass) ?: return@runCatching null
        if (ownerAccess.version.invoke(owner) != HmcMaterialProtocol.VERSION ||
            HmcMaterialProtocol.role(ownerAccess.state.invoke(owner) as? Int ?: return@runCatching null) != HmcMaterialProtocol.GLASS ||
            ownerAccess.owner.invoke(owner) !== owner) return@runCatching null
        Target(owner, state)
    }.getOrNull()

    /** Called by the existing coalesced settings/scene refresh, once per exact owner. */
    fun refresh(): Int {
        if (refreshing.get() == true) return 0
        refreshing.set(true)
        return try {
            val snapshot = synchronized(owners) { owners.keys.toList() }
            snapshot.count { owner ->
                val target = inspect(owner)
                if (target == null || target.state and HmcMaterialProtocol.CLAIMED == 0) {
                    synchronized(owners) { owners.remove(owner) }
                    false
                } else runCatching { accessors.get(owner.javaClass)?.reapply?.invoke(owner) == true }.getOrDefault(false)
            }
        } finally { refreshing.remove() }
    }
}
