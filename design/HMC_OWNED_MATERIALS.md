# Duo-owned materials use an optional exact-pair provider

Lite admits only the exact `com.zhuhuibin.hypermusiccover.duo.NativeMaterialTarget`
interface, protocol version 1, a registered single glass/blur role, a valid glass
owner and a successful synchronous pair claim. Interface lookup uses `ClassValue`
and boot-loader method signatures. It never loads another module, installs a
class-loading callback, polls for a provider or changes the general module-view
exclusion rules. Missing/old contracts and unregistered instances remain excluded.

The ABI has `int hmcMaterialContractVersion()`, `int hmcMaterialState()`,
`View hmcMaterialOwner()`, `boolean hmcMaterialClaim(int)` and
`boolean hmcMaterialReapply()`. State bits are glass 1, blur 2, follow 4,
attached keyguard scope 8, native AOD frame 16 and pair claim 32.

## First setter ownership prevents double application

Duo registers the exact element/container pair before its native material
setters. Every admitted setter claims before Lite's material permission checks,
including when tuning is disabled. Duo then suppresses player parameter relay
for both members at the final native commit; the native player remains its only
sampler source. The first input is a newly constructed native recipe, not a
player-tuned input. Claim does not re-enter material application.

The existing `ViewMaterialEngine` applies Lite's arithmetic once. Owned setters
never enter the engine's historical baseline, output-array or legacy-blend
stores. Nested legacy blend writes retain the existing dispatch guard.

## Existing material permissions control tuning independently of media

Tuning requires the existing glass master, ordinary notification material range,
lockscreen material permission and Duo's current follow/keyguard bits. The media
permission and actual player activity are not inputs. No new setting is exposed.
The existing notification unification option may select the current ordinary
resource recipe only while tuning is permitted and native AOD bit 16 is absent.
An AOD recipe retains its native frame and sampling selection.

## Settings and scene changes request fresh native input

The existing coalesced card/notification scene refresh invokes each known owner
once through `hmcMaterialReapply()`. This also happens while tuning or its range
is disabled, so cleanup reconstructs today's native recipe with zero Lite
tuning. Neither the notification binder nor historical setter replay owns these
pairs. Claims persist while tuning is disabled.

The owner inventory is a weak-key map with Boolean values. Accessor caches are
class-local, not global strong class maps. Synchronous refresh takes a temporary
owner snapshot and rejects nested refresh, while native setters may safely
rediscover the same owner. A failed callback can retry on the next existing
settings/scene event; there is no timer or polling fallback.

## Host checks do not establish device acceptance

`HmcMaterialPolicyTest` and `HmcOwnedMaterialsTest` cover independent permissions,
native AOD selection, exact-interface admission, role/owner/version rejection,
pair claim, owner refresh deduplication, disabled cleanup, reentrant discovery,
callback retry and positive/negative class-cache resolution. Run in an isolated
dev cache:

```sh
LITE_DEV_CACHE=$DEV_VOLUME/hc-lite-owned-material-provider tools/build.sh :app:testDebugUnitTest :app:assembleDebug :app:minifyReleaseWithR8
```

This source change does not modify the package version, install an APK or operate
the phone. Paired on-device verification remains with the maintainer.
