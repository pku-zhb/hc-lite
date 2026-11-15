// SPDX-License-Identifier: AGPL-3.0-or-later
package com.zhuhuibin.hyperchange.lite.glass

import android.view.View
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.Comparator
import javax.tools.ToolProvider
import org.junit.Assert.*
import org.junit.Test
import z70
import z71

class HmcMaterialMembershipTest {
    @Test fun repackagedUnregisteredAndOldTargetsNeverUseGenericMediaFallback() {
        for (version in listOf(0, 1)) {
            val probe = z71().apply { this.version = version }
            assertTrue(HmcMaterialMembership.contains(probe.javaClass))
            assertSame(HmcMaterialMembership.contract(probe.javaClass), HmcMaterialMembership.contract(probe.javaClass))
            GlassSurfaces.markMediaRoot(probe)
            assertTrue(GlassSurfaces.excluded(probe))
            assertFalse(GlassSurfaces.media(probe))
            assertNull(GlassSurfaces.classify(probe))
            assertEquals(0, probe.reads)
            assertEquals(0, probe.claims)
        }
    }

    @Test fun exactMarkerWithNoAbiMethodsStillExcludesGenericFallback() {
        // Model an older provider in a distinct module loader, with only its kept
        // interface name and an R8 short class name. No production class is loaded.
        val temp = Files.createTempDirectory("lite-old-material-marker-")
        try {
            val marker = temp.resolve("com/zhuhuibin/hypermusiccover/duo/NativeMaterialTarget.java")
            Files.createDirectories(marker.parent)
            Files.writeString(marker, "package com.zhuhuibin.hypermusiccover.duo; public interface NativeMaterialTarget {}")
            val target = temp.resolve("z72.java")
            Files.writeString(target, "public class z72 extends android.view.View implements " +
                HmcMaterialProtocol.INTERFACE + " { public z72() { super(null); } }")
            val compiler = requireNotNull(ToolProvider.getSystemJavaCompiler())
            val androidJar = requireNotNull(requireNotNull(View::class.java.protectionDomain).codeSource)
                .location.toURI().let { java.nio.file.Path.of(it) }
            assertEquals(0, compiler.run(null, null, null, "-proc:none", "-classpath", androidJar.toString(),
                "-d", temp.toString(), marker.toString(), target.toString()))
            val loader = object : URLClassLoader(arrayOf(temp.toUri().toURL()), View::class.java.classLoader) {
                override fun loadClass(name: String, resolve: Boolean): Class<*> = synchronized(this) {
                    if (name != HmcMaterialProtocol.INTERFACE && name != "z72") return@synchronized super.loadClass(name, resolve)
                    (findLoadedClass(name) ?: findClass(name)).also { if (resolve) resolveClass(it) }
                }
            }
            loader.use {
                val type = it.loadClass("z72")
                val probe = type.getConstructor().newInstance() as View
                assertTrue(HmcMaterialMembership.contains(type))
                assertEquals(0, HmcMaterialMembership.contract(type)!!.declaredMethods.size)
                assertNull(HmcMaterialAccessors().get(type))
                assertNull(HmcOwnedMaterials().claim(probe))
                GlassSurfaces.markMediaRoot(probe)
                assertTrue(GlassSurfaces.excluded(probe))
                assertFalse(GlassSurfaces.media(probe))
                assertNull(GlassSurfaces.classify(probe))
            }
        } finally {
            Files.walk(temp).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
        }
    }

    @Test fun unrelatedShortClassNamesRetainTheExistingNativeMediaAdmission() {
        val native = z70()
        assertFalse(HmcMaterialMembership.contains(native.javaClass))
        assertFalse(GlassSurfaces.excluded(native))
        assertNull(HmcOwnedMaterials().claim(native))
        GlassSurfaces.markMediaRoot(native)
        assertTrue(GlassSurfaces.media(native))
        assertEquals(GlassSurface.NOTIFICATION, GlassSurfaces.classify(native))
    }
}
