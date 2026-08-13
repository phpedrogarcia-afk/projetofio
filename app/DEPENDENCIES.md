# Android dependency inventory — M1–M3

Status: Resolved M3 engineering checkpoint; lockfile and SHA-256 verification metadata generated

Fio M1 has no production network, analytics, crash-reporting, image, navigation,
dependency-injection, advertising, or account SDK.

M3 adds no production or test dependency. Bounded import uses the platform
Storage Access Framework, Kotlin/JDK UTF-8/cryptographic primitives, the
existing AES-GCM boundary, and Room transactions.

| Dependency | Version | Purpose | Data access | License/removal |
|---|---:|---|---|---|
| Android Gradle Plugin | 9.3.1 | Android build/lint/package tooling | Build-time files only | Android SDK terms; replace through reviewed build migration |
| Kotlin/Compose compiler | 2.2.10 | Built-in Kotlin and Compose compilation | Build-time source only | Apache-2.0; tied to accepted native build |
| KSP2 | 2.3.11 | Room code generation | Build-time schema/source only | Apache-2.0; removable with persistence redesign |
| Compose BOM/UI/Material 3 | 2026.06.01 | Native Android interface | In-process UI state | Apache-2.0; accepted UI toolkit |
| Activity Compose | 1.13.0 | Activity/Compose host | Lifecycle only | Apache-2.0 |
| Lifecycle Compose | 2.10.0 | Lifecycle-aware UI state; latest selected version compatible with compileSdk 36 | Lifecycle only | Apache-2.0 |
| Room | 2.8.4 | Local SQLite persistence and migrations | App-internal encrypted records | Apache-2.0; repository boundary contains it |
| kotlinx.coroutines | 1.11.0 | Structured asynchronous work | In-process task state | Apache-2.0 |
| kotlinx.serialization BOM | 1.8.1 | Aligns transitive SavedState runtime with Room 2.8.4 schema serializers; Fio has no serialization API or payload | Transitive binary compatibility only | Apache-2.0; no Fio serialization surface |
| AndroidX Biometric | 1.1.0 | Optional device-owner authorization | Authorization result only; no biometric material | Apache-2.0; app lock is optional |
| AndroidX Fragment | 1.8.9 | Compatibility host for the Activity Result API used by local export | Activity lifecycle and local result only | Apache-2.0; direct pin prevents Biometric 1.1.0 from selecting obsolete Fragment 1.2.5; remove when the upstream graph no longer requires the pin |
| AndroidX WorkManager | 2.11.2 | Persistent, deferrable, unique one-time Return opportunities | Opaque work identity and timing only; no Entry text, ID, date, score, WorkData, progress, output, or network constraint | Apache-2.0; isolated behind `ReturnOpportunityScheduler`; removable in favor of app-open reconciliation |
| AndroidX Test/Espresso/Compose Test/JUnit | pinned in version catalog | Local automated verification | Synthetic test data only | Test-only; not packaged in release runtime |

The resolved graph is pinned in `mobile/gradle.lockfile`, authenticated in
`gradle/verification-metadata.xml`, and governed by `gradle/libs.versions.toml`.
The release manifest has no `INTERNET`, storage, exact-alarm, or foreground-
service permission. WorkManager contributes `WAKE_LOCK`,
`ACCESS_NETWORK_STATE`, and `RECEIVE_BOOT_COMPLETED` for its scheduler runtime;
Fio configures no network constraint and cannot open a network connection
without `INTERNET`. Its optional foreground-service permission and component are
removed because no Return uses long-running work. AndroidX Biometric
merges the normal `USE_BIOMETRIC` and legacy `USE_FINGERPRINT` permissions;
AndroidX Core adds its signature-level dynamic-receiver permission. No
dependency may add a production endpoint or receive journal content.
