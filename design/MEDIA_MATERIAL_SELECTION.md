# Current native media selection

The maintainer reported native media glass rim loss during AOD transitions while the
effective configuration had glass, media and unification enabled. Source review
found that every media effect could trigger a complete normal MediaViewGlassEffect,
including after FullAod, plus a queued replay. Per-setter normal input replacement
also lacked an outer selection boundary. The active configuration proves the
path was enabled, not that it caused every observed native material change.

Normal unification now has a synchronous outer media-effect selection scope.
Nested same-root initialization inherits one captured native context. Normal
inputs require a known awake effect, an awake notification scene, no native AOD
animation, and no selected FullAod effect. Unknown states, native transitions,
FullAod and writes outside a complete effect retain OEM inputs. Whole-effect
normal replay and its queued retry are removed; existing refresh uses the current
native selector. Configuration permission stays separate from current eligibility.

The isolated implementation passed 331 JVM tests, including seven new transaction
regressions, and independent source review. The unified checks are repeated before packaging. These do not establish phone glass/refraction continuity. Paired
recordings with the HMC progress fix must retest ART75/100, lyrics and FULL AOD
entry/exit. Native torch/camera controls are outside this classifier: any remaining
flat circles stay open for further diagnosis rather than being declared fixed.

This change adds no collector, frame-time controller query, alpha manipulation,
new range, UI setting or delayed material retry. It preserves the native type and
current-state ownership; background source selection remains the HMC/native domain.
