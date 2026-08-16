# System Architecture & Technical Specifications: PainMapAI
**3D Human Body Pain-Mapping & AI Clinical Triage System**

---

## 1. Architectural Layers & Unidirectional Data Flow (UDF)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            UI Layer (Jetpack Compose)                       │
│  - Screens: DashboardScreen, PainMap3DScreen, TriageResultScreen, History   │
│  - Components: PainLogBottomSheet, 3DViewportNode, RedFlagBanner, VASlider  │
│  - ViewModels: StateFlow<UiState>, sealed interface UiAction / UiEvent      │
└──────────────────────────────────────▲──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────┴──────────────────────────────────────┐
│                             Domain Layer (Pure Kotlin)                      │
│  - Models: AnatomicalRegion, PainPoint, ClinicalTriageReport, RedFlagAlert  │
│  - Use Cases: LogPainPointUseCase, GenerateAiTriageUseCase, GetHistoryUseCase│
│  - Repository Interfaces: PainRecordRepository, AiTriageRepository          │
└──────────────────────────────────────▲──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────┴──────────────────────────────────────┐
│                              Data Layer                                     │
│  - Local Data Source: In-Memory / Room PainPointDao & Preferences           │
│  - Remote AI Service: GeminiGenerativeAiService (Structured JSON Schema)    │
│  - 3D Model Loader: Filament GLB/GLTF Node Controller                       │
│  - Repository Impls: PainRecordRepositoryImpl, AiTriageRepositoryImpl       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Structured Gemini AI Triage Contract (JSON Schema)

When dispatching anatomical pain points to Gemini AI, the prompt and system instructions enforce the following structured response schema:

```json
{
  "urgencyLevel": "EMERGENCY | HIGH | MODERATE | LOW | ROUTINE",
  "hasRedFlags": true,
  "redFlagAlerts": [
    {
      "title": "Suspected Radiculopathy / Nerve Compression",
      "severity": "HIGH",
      "guidance": "Seek immediate medical evaluation if progressive limb weakness develops."
    }
  ],
  "preliminaryAssessment": "Detailed clinical synthesis of the anatomical pain distribution and sensations.",
  "potentialConditionsToDiscuss": [
    "Cervical Radiculopathy",
    "Trapezius Myofascial Pain Syndrome"
  ],
  "recommendedSpecialties": [
    "Orthopedic Specialist",
    "Neurology",
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

## 3. 3D Anatomical Scene Architecture (SceneView / Filament)
- **Model:** Human Anatomical Body GLTF/GLB or layered anatomical node hierarchy.
- **Raycasting & Hit-Testing:** Taps on the 3D surface identify `AnatomicalRegion` (e.g. `UPPER_BACK_LUMBAR`, `LEFT_KNEE`, etc.) and 3D normalized coordinates `(x, y, z)`.
- **Dynamic Node Markers:** Node entities added dynamically to the SceneView scene tree with color shaders representing pain intensity.

---

## 4. Package Structure

```
com.example.painmap/
├── data/
│   ├── local/              # Local Storage, In-memory cache & Entities
│   ├── remote/
│   │   ├── gemini/         # Gemini Generative AI client, Prompt templates, Response schemas
│   │   └── dto/            # Data Transfer Objects for AI serialization
│   ├── repository/         # Repository implementations
│   └── mapper/             # DTO <-> Domain entity mappers
├── domain/
│   ├── model/              # Pure domain models (PainPoint, AnatomicalRegion, TriageReport, RedFlag)
│   ├── repository/         # Repository interfaces (PainRecordRepository, AiTriageRepository)
│   └── usecase/            # Pure UseCases (AnalyzePainPointsUseCase, SavePainEntryUseCase)
└── ui/
    ├── navigation/         # Navigation Graph & Screen routes
    ├── theme/              # Color, Type, Shape, Theme
    ├── components/         # PainLogBottomSheet, VASlider, SensationFilterChips, RedFlagAlertCard
    └── screens/
        ├── dashboard/      # Main Dashboard with quick stats & start CTA
        ├── painmap/        # 3D SceneView Pain-Mapping Screen with marker placement
        ├── triage/         # AI Triage & Clinical Report Screen
        └── history/        # Historical Pain Timeline Screen
```
