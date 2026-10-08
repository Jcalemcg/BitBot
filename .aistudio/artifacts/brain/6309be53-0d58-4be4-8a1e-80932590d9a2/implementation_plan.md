# OnDeviceML — Training Studio & Backend Engine

A high-tech mobile ML training studio and decision engine built on the OnDeviceML Training Guide (v1.0.0, 2026-10-08). Enables edge engineers to audit hardware feasibility, select optimal training backends across five paths (Paths A–E), and interactively simulate and monitor BitNet LoRA fine-tuning with live loss telemetry, thermal throttling alerts, battery safeguards, and benchmark matrices.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction and visual design have been aligned with your responses from Phase 1:

- **Confirmed Mode**: Interactive training studio featuring both an automated backend selector and real-time training telemetry.
- **Confirmed Deep-Dive Path**: Path A (BitNet on-device LoRA via QVAC Fabric) with rich hyperparameter controls (Rank 8, Alpha 16, TQ1_0 vs TQ2_0, dynamic GPU tiling), thermal monitoring (>1.5× slowdown cooldown), and battery management (<30% auto-pause).
- **Confirmed Visual Aesthetic**: High-tech dark telemetry console with deep midnight slate backgrounds, neon cyan accents, emerald operational indicators, and glowing telemetry charts.

---

## 1. Overview & Core Concept

### What It Does
**OnDeviceML Studio** is an edge AI fine-tuning workbench and hardware feasibility engine for Android. It translates the 2026 OnDeviceML specification into an interactive, high-fidelity experience:
- **Backend Selector**: Inputs model characteristics, parameter count, and target device hardware; outputs the optimal path (**Path A: BitNet LoRA**, **Path B: LiteRT Signatures**, **Path C: Core ML MLUpdateTask**, **Path D: Off-Device PEFT / On-Device Adapter**, or **Path E: ExecuTorch**) along with time estimates, memory footprints, and OOM risk alerts.
- **Path A BitNet LoRA Training Studio**: Configures and drives interactive LoRA fine-tuning for ternary (1.58-bit) LLMs (125M–2.7B) using QVAC Fabric patterns. Features real-time loss tracking, epoch wall-clock monitors, thermal cooldown triggers, battery safety gates, and dynamic optimizer state checkpoints.
- **Hardware Benchmark Reality Matrix**: An interactive comparative explorer modeling the measured Adreno 830 (Galaxy S25), Mali (Pixel 9), and Apple A18 (iPhone 16) results from 125M to 13B models, highlighting practical sweet spots (≤ 1B) and GPU-vs-CPU acceleration gains.
- **Dataset Preparation & Quality Gate**: Validates JSONL instruction datasets against mobile constraints: token count limits (≤ 50k tokens), 80/10/10 stratified splits, deduplication, and degenerate distribution detectors.
- **Path Catalog & Explicit Negative Boundaries**: Production recipes, CLI syntax (`llama-finetune-lora`), format constraints (`neuralnetwork` vs `mlprogram`), and explicit boundaries (no mobile QLoRA, no mobile full-finetune, deprecated ORT training).

### Target Audience
Mobile AI engineers, edge computing researchers, embedded software developers, and ML practitioners assessing on-device training viability and thermal/power budgets on current-generation hardware.

### Key Value
Eliminates guesswork in mobile model adaptation. Provides immediate clarity on what runs on-device today, what fails with OOM, what costs hours of battery, and how to safely orchestrate edge fine-tuning with thermal and battery protection.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Top Bar: Telemetry Status                       │
│     [Device: Galaxy S25 / Adreno 830]   [Battery: 82% ⚡]   [Thermal: 34°C]│
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌──────────────┐
│   Tab 1:     │             │   Tab 2:     │             │   Tab 3:     │
│   Backend    │             │   BitNet     │             │  Benchmarks  │
│   Selector   │             │ LoRA Studio  │             │  & Reality   │
└──────┬───────┘             └──────┬───────┘             └──────┬───────┘
       │                            │                            │
       ▼                            ▼                            ▼
• Model size slider          • Model: 1B (TQ2_0)          • S25 vs Pixel 9 vs
• Model class selector       • Rank: 8, Alpha: 16           iPhone 16 matrix
• Quantization format        • Batch: 128, Tokens: 18k    • Sweet-spot highlighting
• Device profile             • Quality Gate Passed        • GPU vs CPU 11× chart
• Recommended Path (A-E)     • [RUN] [PAUSE] [CHECKPOINT] • OOM boundary alerts
• Time & Memory cost card    • Live Loss & Thermal Canvas
```

1. **Backend Recommendation Flow**:
   - The user selects a target model class (BitNet ternary LLM, standard LLM Gemma-2/Phi-2, or small classifier/regressor) and scale (125M, 350M, 1B, 2.7B, 7B, 13B).
   - The user selects hardware (Galaxy S25 Adreno 830, Pixel 9 Mali, or iPhone 16 A18).
   - The engine computes suitability, highlights the matched path (A through E), gives exact time-per-epoch estimates, and warns of memory walls (e.g. 7B/13B OOM on phones).

2. **BitNet LoRA Interactive Training Flow (Path A)**:
   - User configures LoRA parameters: Rank (4, 8, 16), Alpha (8, 16, 32), Quant Format (TQ2_0 training default vs TQ1_0 memory-saver), Batch Size (64, 128, 256).
   - User reviews Dataset Gate: verifies sample token count (capped at ~50k tokens, e.g. 18k benchmark dataset), 80/10/10 split, dedup score.
   - User presses **Start Fine-Tuning**:
     - System checks Battery Threshold (ensures ≥ 30% and not in low power mode).
     - System monitors live epoch progress, rendering real-time loss curves, step progression, and token throughput.
     - Simulates thermal conditions: if 3 consecutive epochs suffer >1.5× slowdown, triggers an automated Cooldown Pause with visual warning toast.
     - Supports manual **Pause**, **Resume**, and **Save Checkpoint** (persisting full optimizer state).
     - After training, launches **Pre vs Post Eval**: test prompt side-by-side completion showing adapted assistant behavior.

3. **Benchmarks & Reality Explorer Flow**:
   - Interactive matrix table displaying empirical measurements from the Oct 2026 study.
   - Filter by model size (125M to 13B) and chipset.
   - Visual comparison: Adreno 830 vs Mali vs A18 speed differences, and Adreno GPU (1.3 h) vs CPU (11× slower) chart.
   - OOM Warning badges on 7B/13B for phones.

4. **Paths & Technical Recipes Flow**:
   - Tabs for Paths A, B, C, D, and E with copyable CLI flags, code architecture snippets (LiteRT 4 `@tf.function` signatures: `train`, `infer`, `save`, `restore`; Core ML `neuralnetwork` vs `mlprogram` constraints; LiteRT-LM immutable `loraPath`).
   - "Deliberately Not Claimed" banner clearly noting lack of mobile QLoRA, full fine-tuning, and deprecated ORT.

### Visual Identity & Theme
- **Theme Posture**: High-tech cyber telemetry console with dark mode default (`Theme.kt`).
- **Color Palette**:
  - `Background`: Deep Midnight Slate (`#0B0F17`)
  - `Surface`: Obsidian Blue Card (`#131B2A` / `#1A2436`)
  - `Primary Accent`: Glowing Neon Cyan (`#00E5FF`)
  - `Secondary Accent`: Telemetry Emerald (`#00F5A0`)
  - `Warning / Thermal Cooldown`: High-Visibility Amber (`#FFB300`)
  - `Error / OOM Alert`: Precision Crimson (`#FF3B69`)
  - `Text Hierarchy`: Crisp White (`#F0F4F8`), Slate Muted (`#94A3B8`), Dim Hint (`#64748B`)
- **Typography**:
  - Prominent monospace headers and telemetry readouts for loss, epoch time, memory, and tokens.
  - Clean M3 sans-serif for explanatory notes and interactive controls.
- **Micro-Interactions**:
  - Smooth animated progress meters and loss chart canvas drawing.
  - Pulsing status badges (e.g. `● TRAINING`, `⏸ PAUSED (BATTERY < 30%)`, `❄️ COOLDOWN`).
  - Haptic feedback on step completions and safety alerts.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Interactive Telemetry Engine with Real Device Battery Integration
- **Approach**: Integrate real Android `BatteryManager` broadcast receiver to read actual battery level and charging state, alongside simulated thermal/epoch progression calibrated to the empirical benchmark data.
- **Why**: Grounding the battery safety check in the device's actual battery percentage provides real-world utility while accurately simulating the multi-hour epoch timelines of the 2026 study.
- **Alternatives Considered**: Pure static slides (passive, no practical feel for training orchestration) or mock-only data (less satisfying than reading real device power stats).

### Decision 2: Faithfulness to 2026 OnDeviceML Benchmark Figures
- **Approach**: Hardcode the empirical benchmark table (Galaxy S25 Adreno 830, Pixel 9 Mali, iPhone 16 A18 across 125M–13B) directly into the engine's calculation core.
- **Why**: Provides accurate scaling models. When a user tests a 1B model with 18k tokens on Adreno 830, the studio computes exactly ~1h 18m per epoch, showing real-world fidelity.

### Decision 3: Zero Heavy Dependencies
- **Approach**: Pure Jetpack Compose with custom Canvas rendering for loss curves and telemetry gauges. Room for persisting training logs and saved checkpoints.
- **Why**: Keeps APK size lean, build times fast, and zero runtime crashes from unstable chart dependencies.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (Compose)                    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │             TopAppBar: Battery & Thermal Telemetry           │     │
│   └──────────────────────────────────────────────────────────────┘     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │  Tab 1: BackendSelectorScreen    │ Tab 2: BitNetStudioScreen │     │
│   │  Tab 3: BenchmarkMatrixScreen    │ Tab 4: RecipesScreen      │     │
│   └──────────────────────────────────────────────────────────────┘     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   State & Business Logic (ViewModel)                   │
│                                                                        │
│  ┌───────────────────────┐  ┌───────────────────────────────────────┐  │
│  │ BackendSelectorEngine │  │ FineTuningEngine (Path A LoRA Runner) │  │
│  │ - Model & Device Match│  │ - Epoch & Step Coroutine Loop         │  │
│  │ - Time & Memory Estim.│  │ - Thermal Slowdown Detection (>1.5×)  │  │
│  │ - OOM Rules Engine    │  │ - Battery Threshold (<30% pause)      │  │
│  └───────────────────────┘  └───────────────────────────────────────┘  │
│  ┌───────────────────────┐  ┌───────────────────────────────────────┐  │
│  │ DatasetValidator      │  │ CheckpointManager                     │  │
│  │ - 50k Token Cap Check │  │ - Optimizer State & Adapter Save      │  │
│  │ - 80/10/10 Split Test │  │ - Room Entity Persistence             │  │
│  └───────────────────────┘  └───────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    Persistence & System Telemetry                      │
│  • Android BatteryManager (Real battery % & charging state)            │
│  • Room Database (TrainingSessionDao: logs, checkpoints, configs)       │
└────────────────────────────────────────────────────────────────────────┘
```

### Component Hierarchy
- `MainActivity.kt`: Sets up edge-to-edge window insets, theme, and host scaffold.
- `ui/screens/BackendSelectorScreen.kt`: Interactive model & device selector, feasibility score, time/memory calculation, path routing.
- `ui/screens/BitNetStudioScreen.kt`: LoRA hyperparameter controls, dataset quality gate, live loss curve canvas, thermal/battery monitors, step loop, checkpoint controls, pre/post eval.
- `ui/screens/BenchmarkMatrixScreen.kt`: Interactive chipset comparative table, 125M-13B matrix, sweet-spot callout, GPU vs CPU comparison.
- `ui/screens/RecipesScreen.kt`: Clean technical guide for Paths A–E, copyable CLI recipes, constraints, and negative boundaries.
- `data/model/`: Domain data models (`TrainingConfig`, `DeviceProfile`, `ModelSpec`, `TrainingSession`, `Checkpoint`).
- `data/engine/`: `BackendSelector`, `FineTuningSimulator`, `DatasetValidator`, `TelemetryService`.
- `data/local/`: Room database for storing training history and checkpoints.

---

## 5. Verification & Quality Plan

1. **Gradle Compilation**: Run `compile_applet` to ensure zero compilation or syntax errors.
2. **Metadata Sync**: Ensure `metadata.json` and `res/values/strings.xml` both reflect the app name **OnDeviceML**.
3. **Interactive Test Scenarios**:
   - Verify selecting 7B / 13B models triggers OOM warning on mobile devices.
   - Verify selecting BitNet 1B on Galaxy S25 Adreno 830 calculates ~1h 18m per epoch.
   - Verify battery threshold blocks training if below set threshold (or allows simulation toggle for testing).
   - Verify thermal throttling trigger pauses with a cooldown alert if epoch slowdown occurs.
   - Verify checkpoint save and resume restores optimizer state.
