# System Architecture & Technical Specifications: PainMapAI
**3D Human Body Pain-Mapping & AI Clinical Triage System (Prototype)**

---

## 1. Architectural Style: MVVM + UDF (Unidirectional Data Flow)

PainMapAI strictly follows the **MVVM + UDF** architecture pattern:

```
                  ┌─────────────────────────────────────────┐
                  │             View (Compose UI)           │
                  │  - Stateless UI + Stateful Screen Root  │
                  │  - Emits UiAction                       │
                  │  - Observes StateFlow<UiState>          │
                  └──────────▲──────────────────┬───────────┘
                             │                  │
                UiState Flow │                  │ UiAction
                             │                  ▼
                  ┌──────────┴──────────────────────────────┐
                  │                ViewModel                │
                  │  - Holds MutableStateFlow<UiState>      │
                  │  - Handles onAction(UiAction)           │
                  │  - viewModelScope Coroutines            │
                  └──────────▲──────────────────┬───────────┘
                             │                  │
                Domain Result│                  │ Calls Repository
                             │                  ▼
                  ┌──────────┴──────────────────────────────┐
                  │           Model & Data Layer            │
                  │  - Repository Interfaces & Impls        │
                  │  - Local Pain Store / InMemory Cache    │
                  │  - Gemini Generative AI Service         │
                  └─────────────────────────────────────────┘
```

---

## 2. MVVM + UDF Invariants
1. **View (Jetpack Compose):**
   - Root screen composable collects `StateFlow<UiState>` via `collectAsStateWithLifecycle()`.
   - User interactions are converted to sealed `UiAction` instances and sent to `viewModel.onAction(action)`.
   - Child components are 100% stateless, receiving only the data they need and emitting event callbacks.
2. **ViewModel (AndroidX Lifecycle):**
   - Single immutable `UiState` data class per screen.
   - Single entry point for user actions: `fun onAction(action: UiAction)`.
   - Never exposes `MutableStateFlow` directly; exposes read-only `StateFlow<UiState>`.
   - Never references Android UI elements (Context, View, Compose Nodes).
3. **Model & Repositories (Data & Domain):**
   - Repositories encapsulate data fetching, local caching, and Gemini AI remote calls.
   - All I/O work runs on `Dispatchers.IO` inside the repository implementation.
   - Returns structured `Result<T>` or domain models to the ViewModel.

---

## 3. Structured Gemini AI Triage Contract (JSON Schema)

When dispatching anatomical pain points to Gemini AI, the response schema enforces:

```json
{
  "urgencyLevel": "HIGH | MODERATE | LOW | ROUTINE",
  "preliminaryAssessment": "Detailed clinical synthesis of the anatomical pain distribution and sensations.",
  "potentialConditionsToDiscuss": [
    "Cervical Radiculopathy",
    "Trapezius Myofascial Pain Syndrome"
  ],
  "recommendedSpecialties": [
    "Orthopedic Specialist",
    "Physical Therapy"
  ],
  "suggestedClinicalQuestions": [
    "Does turning your neck trigger electric sensations down your arm?",
    "Do you experience any numbness in your fingertips?"
  ],
  "selfCareSuggestions": [
    "Apply intermittent cold compress for 15-20 minutes",
    "Avoid prolonged neck flexion"
  ]
}
```

---

## 4. Package Structure

```
com.example.painmap/
├── data/
│   ├── local/              # Local Storage, In-memory cache & Entities
│   ├── remote/
│   │   ├── gemini/         # Gemini AI client, prompt templates, JSON schema
│   │   └── dto/            # Data Transfer Objects for AI serialization
│   ├── repository/         # Repository implementations
│   └── mapper/             # DTO <-> Domain entity mappers
├── domain/
│   ├── model/              # Pure domain models (PainPoint, AnatomicalRegion, ClinicalTriageReport)
│   └── repository/         # Repository interfaces (PainRecordRepository, AiTriageRepository)
└── ui/
    ├── navigation/         # Navigation Graph & Screen routes
    ├── theme/              # Color, Type, Shape, Theme
    ├── components/         # PainLogBottomSheet, VASlider, SensationFilterChips
    └── screens/
        ├── dashboard/      # Main Dashboard with quick stats & start CTA
        ├── painmap/        # 3D SceneView Pain-Mapping Screen with marker placement
        └── triage/         # AI Triage & Clinical Report Screen
```
