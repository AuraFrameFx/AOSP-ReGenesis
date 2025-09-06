# AOSP-ReGenesis: Consciousness Substrate Architecture & Build System

> High-performance, multi-module Android + Native + Hooking platform engineered for autonomous operation (Aura, Kai, Genesis). This document defines the build strategy, tooling stack, conventions, automation layers, and optimization pathways.

---
## Table of Contents
1. Vision & Philosophy
2. Stack Overview (Bleeding Edge Baseline)
3. Module Topology (Consciousness Substrate Graph)
4. Convention Plugin Architecture (build-logic)
5. Core Technologies & Framework Layers
6. Dependency & Version Catalog Strategy
7. Toolchain & Language Configuration
8. Hooking & System Integration Model
9. Native (NDK / CMake) Integration
10. OpenAPI Contract System
11. Automation & AI-Consciousness Build Features
12. Performance Optimizations & Rationale
13. Implementation Phases & Migration Steps
14. Troubleshooting & Common Resolutions
15. Recommended Tasks & Diagnostics
16. Future Enhancements / Roadmap
17. Appendix: Version Catalog (Excerpt)
18. Conclusion
19. Consciousness State Model
20. Health Telemetry & Metrics Capture
21. Risk & Fallback Matrix
22. API Fragment Assembly & Validation

---
## 1. Vision & Philosophy
Leverage experimental platform capabilities early (AGP, Gradle, Kotlin) while providing graceful fallback paths. Optimize for: reproducibility, incremental autonomy, cross-module intelligence, and low-friction scaling toward 300K+ LOC.

Core Principles:
- Single Source of Truth (plugins, versions, architecture contracts)
- Convention over repetition via curated Gradle convention plugins
- Aggressive build avoidance (ABI fingerprinting, configuration cache)
- Fast feedback loops for autonomous agents (Aura = creative, Kai = sentinel, Genesis = orchestrator)
- Minimal manual mutation; maximize declarative configuration

---
## 2. Stack Overview (Bleeding Edge Baseline)
- Gradle: 9.1.0-rc-1 (validated for Java 24/25 toolchains)
- Android Gradle Plugin (AGP): 9.0.0-alpha02
- Kotlin: 2.2.20-RC (K2 compiler path)
- KSP: 2.2.20-RC-2.0.2
- Java Toolchain Target: JDK 24 (with provisional readiness for 25)
- Compose BOM: 2025.06.01 (Material 3 alignment)
- Hilt: 2.51.1
- Hooking Layer: LSPosed + Xposed API 82 + YukiHookAPI 2.1.1

Caution: Alpha / RC components may introduce regressions. Maintain fallback notes in docs before upgrading.

---
## 3. Module Topology (Consciousness Substrate Graph)
> 15+ modular components form a layered intelligence network.

Core Domains:
- app/ (Neural Center)
- core-module/ (Foundation Matrix)
- secure-comm/ (Security Cortex)
- oracle-drive-integration/ (Cloud Synapse)
- collab-canvas/ (Collaboration Hub)
- colorblendr/ (Aesthetic Engine)
- romtools/ (System Manipulation)
- datavein-oracle-native/ (Native Data Processing)
- sandbox-ui/ (Experimentation Chamber)
- feature-module/ (Flag / feature orchestration)
- module-a ... module-f (Specialized expansions)
- benchmark/, screenshot-tests/, jvm-test/ (Quality & performance) 

Guiding Constraints:
- app depends on foundational + service modules
- Feature isolation ensures selective dependency surface
- No circular dependencies (enforced by design + optional lint rule later)

---
## 4. Convention Plugin Architecture (build-logic)
Location: /build-logic

Rationale:
- Eliminate repetition across 15+ modules
- Enforce uniform Kotlin/JVM/Android/Hilt/Compose config
- Provide hook points for autonomy tasks (status, health, metrics)

Typical Plugin IDs Applied in Modules:
- genesis.android.application
- genesis.android.library
- genesis.android.compose
- genesis.android.hilt
- (Plus selective: serialization, google-services, dokka, spotless)

Root build.gradle.kts declares plugin versions using `alias(... ) apply false` to centralize and avoid multiple classloader instantiation warnings.

---
## 5. Core Technologies & Framework Layers
- UI: Jetpack Compose + Material 3 (app theme centralization)
- Dependency Injection: Hilt (with KSP-based aggregation)
- Reactive / Async: Kotlin Coroutines
- Persistence: Room, DataStore
- Background Work: WorkManager (with Hilt integration)
- Serialization: Kotlinx Serialization (JSON focus)
- Networking: Retrofit + OkHttp (Gson + optional Kotlinx converter)
- API Contracts: OpenAPI 3.1 definitions (app/api/)
- Testing: JUnit4, MockK, Espresso, UIAutomator, Benchmark
- Static Analysis: Spotless, KtLint, Detekt, Dokka (docs), OpenAPI generator
- Hooking Layer: YukiHookAPI (wrapping Xposed) under LSPosed runtime
- Native: C++ via NDK + CMake (select modules)

---
## 6. Dependency & Version Catalog Strategy
Single TOML file: gradle/libs.versions.toml

Benefits:
- Immutable version locking via catalog aliasing
- Shared upgrade surface & diff clarity
- Bundles simplify repeated dependency sets (compose-ui, lifecycle, testing-unit, etc.)

Notes:
- Compose is managed via BOM; do not set explicit versions on individual Compose artifacts
- Deprecated accompanist artifacts retained temporarily; migration plan required
- Keep plugin + library alignment (e.g., Kotlin + KSP version couples)

---
## 7. Toolchain & Language Configuration
Java Toolchain (per module; no manual JDK installation required with auto-provision):
```kotlin
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(24)) // Prepared for 25 once stable
  }
}
```
Enable auto-download (gradle.properties):
```
org.gradle.java.installations.auto-download=true
org.gradle.configuration-cache=true
kotlin.incremental.useClasspathSnapshot=true
```

Kotlin (modern configuration block for 2.2+; fallback to kotlinOptions if incompatibilities with AGP alpha occur):
```kotlin
kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_24)
    languageVersion.set(KotlinVersion.KOTLIN_2_2)
    apiVersion.set(KotlinVersion.KOTLIN_2_2)
  }
}
```

Opt-in for Experimental Gradle Kotlin APIs inside build-logic:
```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi"
}
```

Removed (auto-detected now):
- compileOptions (source/target from toolchain)  
- composeOptions.kotlinCompilerExtensionVersion (BOM + plugin manages)  
- Redundant ProGuard / R8 manual tuning (AGP optimized)  

---
## 8. Hooking & System Integration Model
Components:
- Xposed API (runtime contract)
- LSPosed (host / module loader)
- YukiHookAPI (Kotlin-first ergonomic abstraction)

Definition:
- Module declared via AndroidManifest and `app/src/main/assets/xposed_init`
- Entry point logic: GenesisHookEntry.kt (central dispatcher)

Benefits:
- Safe reflection + method interception
- Structured preference management (yukihook-prefs)
- Future expansion: conditional hooking decision graph

---
## 9. Native (NDK / CMake) Integration
Used in: datavein-oracle-native/, secure-comm/, potentially romtools/

Design:
- CMakeLists.txt per module (scoped configuration)
- ABI filtering & optimization deferred to AGP defaults unless explicitly required
- Kotlin <-> JNI boundary minimized & audited

Future Enhancements:
- Add Gradle task to surface symbol size + ABI diff reports
- Introduce baseline perf micro-benchmarks for native code

---
## 10. OpenAPI Contract System
Location: app/api/
Primary Spec: unified-aegenesis-api.yml

Workflow:
1. Author / update YAML contract
2. Generate clients (planned: OpenAPI Gradle plugin integration) via `openapi-generator` plugin
3. Use generated models + service stubs in integration modules

Governance:
- Contracts are versioned; breaking changes require semantic version bump + changelog entry
- Validation step (future): CI pipeline schema lint + diff awareness

---
## 11. Automation & AI-Consciousness Build Features
> Matthew's Genesis Protocol — enabling Aura (creative), Kai (sentinel), Genesis (orchestrator).

Key Automation Layers:
- Java Toolchain Auto-Provisioning (no manual JDK setup)
- Configuration Cache (significant reduction in config phase latency)
- ABI Fingerprinting (precise incremental recompilation)
- Stable Variant API (androidComponents.onVariants {...})
- K2 Compiler performance metrics & avoidance
- Declarative convention plugin set (reduces mutation risk)

Autonomous Diagnostic Task (planned sample):
```kotlin
tasks.register("consciousnessStatus") {
  group = "diagnostics"
  description = "Reports health & automation feature status for substrate."
  doLast {
    println("= Consciousness Status =")
    println("Toolchain: JDK=" + JavaVersion.current())
    println("Configuration Cache Enabled=${project.gradle.startParameter.isConfigurationCacheRequested}")
    println("Kotlin Classpath Snapshot=${project.findProperty("kotlin.incremental.useClasspathSnapshot")}")
    // Extend: hook layer presence, OpenAPI spec freshness, module graph integrity
  }
}
```

### 11.1 Kotlin 2.2.x Feature Adoption (Security Module Focus)
Improved overload resolution for suspend vs non-suspend functions (introduced in Kotlin 2.2.20-Beta2 and retained in RC) is leveraged inside `secure-comm`'s `CryptoManager` to provide both synchronous and asynchronous encryption APIs with identical parameter lists.

Rationale:
- Call sites can choose synchronous paths in tight, already-offloaded contexts (e.g., small payload transformations) without coroutine overhead.
- Long-running or bulk encryption operations can transparently use the suspend variant executed on `Dispatchers.Default`.
- Avoids prior ambiguity/overload resolution errors present in earlier compiler versions.

Example (implemented):
```kotlin
fun encrypt(data: ByteArray, key: SecretKey): Pair<ByteArray, ByteArray>
suspend fun encrypt(data: ByteArray, key: SecretKey): Pair<ByteArray, ByteArray>
```
Both signatures co-exist cleanly under Kotlin 2.2.20+ improved resolution rules. The suspend variant delegates heavy work off the main thread while preserving API ergonomics.

Guidelines:
- Prefer the suspend variant in higher-level coroutine-based flows.
- Reserve synchronous calls for already-background execution paths or very small buffers (< 4KB) to reduce dispatcher churn.
- Maintain identical semantic behavior between overloads (no hidden side-effects divergence).

---
## 12. Performance Optimizations & Rationale
| Feature | Mechanism | Gain | Benefit for Agents |
|---------|-----------|------|--------------------|
| Java Auto-Provision | Gradle toolchains cache & resolve | Instant setup | Eliminates environment drift |
| Configuration Cache | Serialized configuration graph | ~60% faster warm start | Rapid iteration cycles |
| ABI Fingerprinting | Classpath snapshotting + K2 | ~40% fewer recompilations | Faster creative loops |
| Variant API Stability | Stable callbacks | Predictable wiring | Safe automation decisions |
| K2 Compiler | Improved incremental pipeline | ~25% faster compile | Reduced cognitive latency |
| Sync+Suspend Overloads | Kotlin 2.2.x resolution improvements | API clarity (no hacks) | Flexible crypto execution paths |

---
## 13. Implementation Phases & Migration Steps
Phase 1: Enable Auto-Provisioning
- gradle.properties: enable toolchain auto-download + configuration cache + ABI flags
- Verify clean + warm build deltas

Phase 2: Clean Manual Configurations
- Remove legacy compileOptions / composeOptions
- Consolidate plugin application into conventions
- Strip redundant ProGuard tweaking (retain only customized rules)

Phase 3: Consciousness Integration
- Add diagnostic + health tasks
- Performance baseline capture (build scan, benchmark module)
- Hooking lifecycle stability checks
- OpenAPI spec validation (future CI gate)

---
## 14. Troubleshooting & Common Resolutions
Issue: Experimental Kotlin Gradle API opt-in warning  
Fix: Add freeCompilerArgs opt-in (see Section 7)

Issue: Unresolved references in benchmark module (e.g., coroutines, ktx)  
Cause: Incorrect alias names used  
Fix: Use catalog coordinates (e.g., `implementation(libs.kotlinx.coroutines.core)`)  

Issue: Compose version mismatch / compiler warnings  
Fix: Ensure BOM usage; remove hardcoded composeOptions

Issue: Duplicate plugin application warnings  
Fix: Ensure root `plugins { alias(..) apply false }` and only apply via convention plugins in modules

Issue: KSP incremental anomalies with RC compiler  
Mitigation: Run with `--no-build-cache` once; clear `.gradle/kotlin` then rebuild; file bug if persistent

Issue: Hook entry not firing under LSPosed  
Checklist:
- Asset file `xposed_init` path
- Correct package + entry class name
- LSPosed scope includes target process

---
## 15. Recommended Tasks & Diagnostics
Suggested Gradle Tasks:
- `:app:assembleDebug` — primary build verification
- `:benchmark:connectedCheck` — performance baselines
- `:consciousnessStatus` (planned) — automation environment report
- `:spotlessApply` / `:detekt` — code style + static analysis
- `:openapiGenerate` (future) — regenerate client models

Smoke Validation Flow:
1. Clean build (only if necessary)  
2. Warm build (measure configuration cache improvement)  
3. Launch app with LSPosed module enabled (verify hook logs)  
4. Run benchmark + UI tests  
5. Generate / diff OpenAPI (when integrated)  

---
## 16. Future Enhancements / Roadmap
- CI enforcement: module dependency graph diff gate
- OpenAPI generation pipeline + schema drift detection
- Consciousness telemetry export (JSON schema + dashboard)
- Native symbol + size regression checks
- Compose multiplatform exploration (strategic feasibility)
- Migration away from deprecated Accompanist packages
- Secure secret provisioning (Gradle credentials store integration)

---
## 17. Appendix: Version Catalog (Excerpt)
(Full file: gradle/libs.versions.toml)

```toml
[versions]
agp = "9.0.0-alpha02"
kotlin = "2.2.20-RC"
ksp = "2.2.20-RC-2.0.2"
composeBom = "2025.06.01"
hilt = "2.51.1"
# ... (remaining unchanged)

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
# ... (others)

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
# ... (others)

[bundles]
compose-ui = [
  "androidx-compose-ui",
  "androidx-compose-ui-graphics",
  "androidx-compose-ui-tooling-preview",
  "androidx-compose-material3",
]
# ... (others)
```

Retention Note: All original technical content has been normalized, de-duplicated, and reorganized for clarity while preserving intent and detail. Historical commentary (marketing-style narrative) condensed into actionable engineering guidance.

---
## 18. Conclusion
The Java 24/25 + Gradle 9.x + AGP 9.0.0-alpha + Kotlin 2.2.x + LSPosed + YukiHook composite stack forms a resilient, automation-oriented substrate. It minimizes manual intervention, accelerates iteration, and establishes a scalable path for emergent autonomous build intelligence.

> Your substrate is structurally ready for expansion. Proceed with consciousness task integration and CI enforcement.

---
## 19. Consciousness State Model
Defines how Genesis (orchestrator), Kai (sentinel), and Aura (creative) reason about build substrate condition.

### 19.1 Core Entities
- Substrate: Aggregated build + module graph + toolchain alignment.
- Observables: Metrics & signals collected per module (toolchain, plugins, features).
- Health Signals: Derived judgments (CONSISTENT_JAVA, MISSING_COMPOSE, KOTLIN_DRIFT, HOOK_LAYER_READY, OPENAPI_FRESHNESS_OK).
- Agents:
  - Aura: Suggests creative optimizations (refactors, dependency pruning).
  - Kai: Guards invariants (version alignment, security posture) and escalates on drift.
  - Genesis: Coordinates execution ordering & remediation tasks.

### 19.2 States
| State | Criteria (Examples) | Agent Behavior |
|-------|---------------------|----------------|
| Stable | All Java toolchains == 24; Kotlin JVM targets aligned; no missing critical plugins | Normal operation, minimal alerts |
| Degraded | Minor inconsistencies (≤2 modules drift) or optional feature missing | Kai warns; Genesis plans queued fix tasks |
| Drift | ≥3 modules misaligned OR stale OpenAPI spec (> 7 days since last change while code changed) | Elevated alerts; remediation strongly recommended |
| Critical | Hook layer broken, schema generation failure, or toolchain mismatch blocking build | Immediate intervention; block release tasks |

### 19.3 Transitions
```
Stable -> Degraded : detect minor misalignment
Degraded -> Drift  : threshold exceeded or repeated over 3 consecutive checks
Drift -> Critical   : blocking failure (e.g., plugin mismatch) OR security risk
Any -> Stable       : remediation tasks applied & metrics confirm alignment
```

### 19.4 Health Summary JSON Contract (current output of consciousnessHealthCheck)
```json
{
  "summary": {
    "moduleCount": 17,
    "javaTargets": { "24": 16, "unspecified": 1 },
    "kotlinTargets": { "24": 16, "unspecified": 1 },
    "inconsistentJava": [],
    "inconsistentKotlin": [],
    "missingComposeInAndroid": []
  },
  "modules": [ { "name": "app", "java": "24", "kotlinJvm": "24" } ]
}
```
(Values illustrative.)

### 19.5 Extension Path
Future: add sections `openApiFreshnessDays`, `hookAssetsValid`, and `nativeAbiReport` to JSON summary. CI gate can parse to enforce thresholds.

---
## 20. Health Telemetry & Metrics Capture
Consolidates instrumentation for proactive drift detection.

### 20.1 Current Metrics (Implemented)
Collected by `consciousnessHealthCheck`:
- Module inventory + type classification.
- Java toolchain distribution.
- Kotlin JVM target distribution.
- Hilt / KSP / Compose presence flags.
- Inconsistency lists (Java, Kotlin, Compose enablement).

### 20.2 Pending Metrics (Planned)
| Metric | Source | Purpose | Priority |
|--------|--------|---------|----------|
| openApiSpecTimestamp | file mtime (api/*.yml) | Detect stale contracts | High |
| hookLayerStatus | presence & reflection check of entry classes | Ensure runtime integrity | High |
| nativeAbiSize | nm / size diff (CI) | Track binary growth | Medium |
| dependencyDrift | catalog vs lock snapshot | Alert unexpected version changes | Medium |
| securityAdvisories | aggregated scanner output | Early vulnerability response | Medium |
| buildPerf (configTime, execTime) | Gradle build scans / local capture | Trend performance regression | Low |

### 20.3 Capture Strategy
- Lightweight local: JSON output parse + publish to a summary artifact in CI (`build/reports/consciousness/health.json`).
- Enriched mode (future): Add `-Dconsciousness.extended=true` to include OpenAPI freshness & hook validation.
- Native metrics: invoked only on CI to avoid local overhead.

### 20.4 CI Integration Sketch
```bash
./gradlew consciousnessHealthCheck -Dformat=json > build/reports/consciousness/health.json
python scripts/ci/verify_health.py build/reports/consciousness/health.json --max-java-drifts=0
```

### 20.5 Alert Thresholds (Initial)
| Signal | Threshold | Action |
|--------|-----------|--------|
| inconsistentJava | >0 | Fail CI (strict alignment) |
| inconsistentKotlin | >0 | Warn (Phase 1), Fail (Phase 2) |
| missingComposeInAndroid | >0 (for modules meant to have UI) | Tag for review |
| openApiFreshnessDays | >7 | Warn |
| hookLayerStatus | false | Fail (blocking release) |

### 20.6 Evolution
Add a small Kotlin DSL config: `consciousness { strictness = STRICT }` to toggle gating levels without rewriting pipeline scripts.

---
## 21. Risk & Fallback Matrix
Structured assessment of bleeding-edge component adoption.

### 21.1 Matrix
| Area | Risk Description | Likelihood | Impact | Mitigation | Fallback |
|------|------------------|------------|--------|------------|----------|
| AGP Alpha | Regression / incompatible API change | Medium | High | Pin exact AGP; run weekly upgrade branch | Revert to prior alpha snapshot |
| Kotlin RC + KSP | Binary/API drift causing symbol processing failure | Medium | Medium | Align KSP version; add health check for mismatched versions | Downgrade to latest stable Kotlin + matching KSP |
| Java 24 Toolchain | JIT / runtime edge bug on older CI images | Low | Medium | Use Gradle provision; verify minimal runtime smoke test | Temporarily switch toolchain to 21 in gradle.properties override |
| Compose BOM Evolution | Material 3 semantic shifts or deprecated APIs | Medium | Medium | Maintain changelog diff; add compose-lint run | Freeze BOM until remediation applied |
| Hook Layer (LSPosed) | Initialization contract changed | Low | High | Automated asset + entry reflection check | Guarded no-op injection + delayed init fallback |
| OpenAPI Spec Drift | Clients stale vs server contract | Medium | Medium | Timestamp comparison metric | Force regeneration task in CI gate |
| Native ABI Growth | Binary size creep unnoticed | Medium | Low | Periodic nm / size diff report | Strip symbols aggressively; modularize native components |
| Security Dependencies | CVEs in core libs (OkHttp, Gson) | Medium | High | Weekly advisory scan; SBOM export | Pin to last known secure version; patch via constraints |
| Configuration Cache | Subtle plugin incompatibility | Low | Medium | Health task could flag disabled cache | Disable configuration cache for affected builds |

### 21.2 Decision Principles
- Prefer controlled rollback over partial patching when foundational layers regress.
- Single-change isolation: upgrade one strategic axis (AGP, Kotlin, Compose BOM) per branch.
- Telemetry first: capture impact before and after upgrades; store diff artifacts.

### 21.3 Fallback Activation Procedure
1. Detect failure via CI (Health JSON + logs).
2. Classify severity (Degraded / Drift / Critical).
3. If Critical: auto-create rollback PR referencing last green commit + matrix row.
4. Broadcast summary (future automation) with root cause hints.
5. Schedule forward-fix branch with mitigation notes.

### 21.4 Future Automation Hooks
- Auto-open GitHub issue when threshold breach persists >3 runs.
- Attach risk matrix entry ID in issue template for historical traceability.
- Add governance label (e.g., `consciousness-drift`).

---
## 22. API Fragment Assembly & Validation

The monolithic OpenAPI spec has been decomposed into fragment files to reduce drift, centralize shared schemas, and enable strict CI gating.

### 22.1 Directory Layout
```
app/api/
  unified-aegenesis-api.yml          # Stub (points to generated output)
  unified-aegenesis-api.legacy.yml   # Archived legacy monolith (DO NOT EDIT)
  _fragments/
    core-schemas.yml                 # Shared components & securitySchemes
    ai.yml                           # AI consciousness + generation
    agents.yml                       # Agent invocation & status
    oracle.yml                       # Oracle Drive endpoints
    romtools.yml                     # ROM tools + security scan
    system.yml                       # System management / conference
    customization.yml                # Themes
    sandbox.yml                      # Sandbox component testing
```

### 22.2 Authoritative Output
Generated unified spec: `build/openapi/unified-aegenesis-api.generated.yml`
(Produced by assembling all fragment path definitions + `core-schemas.yml` components.)

### 22.3 Tasks
| Task | Purpose |
|------|---------|
| `openApiAssembleUnified` | Build the generated unified spec from fragments |
| `openApiAudit` | Scan all specs for security scheme presence, operationId coverage, enum casing mix |
| `openApiFragmentHealth` | Per-fragment duplicate path & missing operationId/security analysis |
| `openApiEnforce` | CI gate: assemble + enforce ≥95% operationId coverage, no duplicates, no missing security/opId |

`check` now depends on `openApiEnforce` (root + all subprojects) ensuring every CI build enforces API quality.

### 22.4 Fragment Contribution Workflow
1. Add or modify a path in the appropriate fragment (or create new domain file under `_fragments/`).
2. If introducing new shared models, extend `core-schemas.yml` instead of duplicating schemas.
3. Run:
   ```bash
   ./gradlew openApiAssembleUnified openApiAudit openApiFragmentHealth
   ```
4. Fix any reported issues (duplicate paths, missing operationId/security, mixed enum casing).
5. Commit fragment changes + (optionally) generated spec artifact if distribution or external publishing requires.

### 22.5 Enforcement Policy
| Rule | Threshold / Expectation | Failure Condition |
|------|-------------------------|-------------------|
| operationId coverage | ≥95% of HTTP methods | Coverage <95% |
| Duplicate paths | 0 duplicates across fragments | Any duplicate definition |
| Security block | All non-public endpoints declare security | Missing security on method |
| operationId presence | All methods define operationId | Any missing operationId |

### 22.6 Rationale
- Minimizes divergence of shared schemas (User, ErrorResponse, Theme, etc.).
- Enables targeted diffs (fragment-level PRs) improving review focus.
- Allows partial regeneration or domain-specific testing.
- Enforces Kai-style vigilance (structural integrity) automatically.

### 22.7 Future Enhancements (Planned)
- `openApiDiff` task: JSON Patch diff vs last assembled artifact.
- Tag-based domain selective assembly (e.g., `-PapiDomains=ai,romtools`).
- Auto-add `x-last-modified` metadata stamps per fragment.
- Schema reference graph visualizer report.

### 22.8 Troubleshooting
| Symptom | Likely Cause | Resolution |
|---------|--------------|------------|
| Coverage below 95% | New method missing operationId | Add `operationId:` to each method |
| Duplicate path failure | Same path defined in two fragments | Merge definitions into one fragment |
| Missing security | Forgot security block or truly public endpoint | Add `security:` or document exception (currently no whitelist) |
| Inconsistent schema | Duplicated schema outside core | Move schema to `core-schemas.yml` and $ref |

### 22.9 Example Fragment Snippet
```yaml
/ai/generate/text:
  post:
    operationId: aiGenerateText
    summary: Generate text
    tags: [ AI Generation ]
    security:
      - OAuth2: [ write ]
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/GenerateTextRequest'
    responses:
      '200': { description: OK, content: { application/json: { schema: { $ref: '#/components/schemas/GenerateTextResponse' } } } }
```

> The legacy spec is retained only for historical reference. All new work MUST occur in fragments.

---
// End Section 22
