# OnDeviceML — Hugging Face Model & Dataset Hub Integration

Seamlessly connecting OnDeviceML with the official Hugging Face Hub, enabling users to explore mobile-compatible models and instruction datasets, audit them against their scanned hardware, and import validated datasets directly into the BitNet LoRA Training Studio.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction has been aligned with your confirmed choices:

- **Hub Browsing**: Live search directly against the official Hugging Face REST API (`https://huggingface.co/api/models` and `https://huggingface.co/api/datasets`), backed by curated mobile-ready presets (`1bitLLM/bitnet_b1_58-1B`, `tetherto/qvac-fabric-llm-bitnet`, `google/gemma-2-2b`, Alpaca, No Robots, etc.).
- **Authentication Strategy**: Completely open public access by default (no token required). An optional User Access Token setting is available for gated or private repositories.
- **Automated Dataset Action**: Selecting a Hugging Face dataset automatically runs the mobile token budget gate (≤ 50k tokens, 80/10/10 split) and loads it into the BitNet Studio training pipeline.

---

## 1. Overview & Core Concept

### What It Adds
1. **Hugging Face Hub API Client (`HuggingFaceService.kt`)**:
   - Built with Retrofit & Moshi:
     - Model Search & Details: Queries model cards, downloads count, likes, parameter counts, and pipeline tags (`text-generation`, `bitnet`, `gguf`).
     - Dataset Search & Details: Queries dataset cards, estimated rows/tokens, task categories, and license information.
     - Optional Bearer token injection (`Authorization: Bearer hf_...`).
     - Resilient networking with graceful offline fallbacks and mobile-curated presets.

2. **Hugging Face Hub Explorer Screen (`HuggingFaceHubScreen.kt`)**:
   - **Models Tab**:
     - Search and filter by tag (`#bitnet`, `#gguf`, `#1b`, `#mobile`).
     - Real-time compatibility badge against the user's scanned hardware (`✓ Fits in RAM` or `🔒 Exceeds RAM`).
     - One-tap "Deploy to Studio" action.
   - **Datasets Tab**:
     - Search instruction tuning datasets (`#instruction-tuning`, `#alpaca`, `#chat`).
     - Displays token estimates and sample sizes.
     - "Validate & Load into Studio" action that checks mobile token limits.
   - **Token Settings Modal**:
     - Simple dialog to optionally paste a Hugging Face User Access Token.

3. **Direct Pipeline Integration with BitNet Studio**:
   - Selected Hugging Face models pass their repository ID, parameter count, and quantization tags directly into the Studio's hyperparameter engine.
   - Selected Hugging Face datasets immediately update the Studio's active dataset and Quality Gate inspection card.

---

## 2. User Experience & Navigation

```
┌────────────────────────────────────────────────────────────────────────┐
│                   HUGGING FACE HUB // EDGE REPOSITORIES                │
│   [🔍 Search Hugging Face...]       [🔑 HF Token: Public / Optional]   │
│                                                                        │
│   ┌───────────────────────────────┐ ┌────────────────────────────────┐ │
│   │     ● MODELS (Live Search)    │ │           DATASETS             │ │
│   └───────────────────────────────┘ └────────────────────────────────┘ │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┴────────────────────────────┐
       ▼                                                         ▼
┌───────────────────────────────────┐     ┌───────────────────────────────────┐
│ 1bitLLM/bitnet_b1_58-1B           │     │ iamtarun/python_code_18k_alpaca   │
│ • Format: TQ2_0 GGUF · 1B params  │     │ • 18,240 tokens · 300 docs        │
│ • Downloads: 142k · Likes: 1.2k   │     │ • Split: 80/10/10 Stratified      │
│ [✓ FITS IN DEVICE RAM (Tier A)]   │     │ [✓ UNDER 50K MOBILE TOKEN CAP]    │
│ [ IMPORT INTO BITNET STUDIO ]     │     │ [ VALIDATE & LOAD INTO STUDIO ]   │
└───────────────────────────────────┘     └───────────────────────────────────┘
```

- **Navigation Bar**: Adds a dedicated **`HF Hub`** tab between the Studio and Benchmarks (Selector, Studio, HF Hub, Benchmarks, Recipes).

---

## 3. Technical Architecture & File Plan

1. **Permissions (`AndroidManifest.xml`)**:
   - Add `<uses-permission android:name="android.permission.INTERNET" />`
   - Add `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
2. **Data Layer (`data/huggingface/`)**:
   - `HuggingFaceModels.kt`: Data transfer objects for HF model and dataset search responses.
   - `HuggingFaceApi.kt`: Retrofit interface defining search and detail endpoints.
   - `HuggingFaceRepository.kt`: Repository coordinating live network queries, token header injection, and curated mobile fallbacks.
3. **UI Layer (`ui/screens/HuggingFaceHubScreen.kt`)**:
   - Search bar with instant filter chips (`#bitnet`, `#gguf`, `#mobile-ready`, `#instruction`).
   - Model cards with real-time RAM compatibility check.
   - Dataset cards with one-tap token validation and Studio injection.
   - Token config sheet.
4. **App Integration**:
   - `MainActivity.kt`: Update `AppTab` enum and navigation scaffold to host the HF Hub tab.

---

## 4. Verification & Testing Plan

1. **Gradle Compilation**: Run `compile_applet` to ensure zero compilation or syntax errors.
2. **Network Query & Offline Resilience**: Verify search and curation gracefully display items both online and offline.
3. **Token Validation Flow**: Verify selecting a Hugging Face dataset updates the active dataset in the BitNet LoRA Studio.
4. **Unit Tests**: Add tests verifying HF model parsing, token validation, and repository fallback logic.
