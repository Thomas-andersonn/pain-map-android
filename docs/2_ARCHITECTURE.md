# System Architecture & Technical Specifications: PainMapAI
**3D Human Body Pain-Mapping & AI Clinical Triage System (Prototype)**

---

## 1. Architectural Layers & Unidirectional Data Flow (UDF)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            UI Layer (Jetpack Compose)                       │
│  - Screens: DashboardScreen, PainMap3DScreen, TriageResultScreen            │
│  - Components: PainLogBottomSheet, 3DViewportNode, VASlider, SensationChips │
│  - ViewModels: StateFlow<UiState>, sealed interface UiAction / UiEvent      │
└──────────────────────────────────────▲──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────┴──────────────────────────────────────┐
│                             Domain Layer (Pure Kotlin)                      │
│  - Models: AnatomicalRegion, PainPoint, ClinicalTriageReport                │
│  - Use Cases: LogPainPointUseCase, GenerateAiTriageUseCase                  │
│  - Repository Interfaces: PainRecordRepository, AiTriageRepository          │
└──────────────────────────────────────▲──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────┴──────────────────────────────────────┐
│                              Data Layer                                     │
│  - Local Data Source: In-Memory / Preferences PainPoint Store               │
│  - Remote AI Service: GeminiGenerativeAiService (Structured JSON Schema)    │
│  - 3D Model Loader: Filament GLB/GLTF Node Controller                       │
│  - Repository Impls: PainRecordRepositoryImpl, AiTriageRepositoryImpl       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Structured Gemini AI Triage Contract (JSON Schema)

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

## 3. 3D Anatomical Scene Architecture (SceneView / Filament)
- **Model:** Human Anatomical Body model in SceneView.
- **Raycasting & Hit-Testing:** Taps on the 3D surface identify `AnatomicalRegion` and 3D normalized coordinates `(x, y, z)`.
- **Dynamic Node Markers:** Node entities added dynamically with color shaders representing pain intensity.

---

## 4. Package Structure

```
com.example.painmap/
├── data/
│   ├── local/              # In-memory store & preferences
│   ├── remote/
│   │   ├── gemini/         # Gemini AI client, prompt templates, JSON schema
│   │   └── dto/            # Data Transfer Objects for AI serialization
│   ├── repository/         # Repository implementations
│   └── mapper/             # DTO <-> Domain entity mappers
├── domain/
│   ├── model/              # Pure domain models (PainPoint, AnatomicalRegion, ClinicalTriageReport)
│   ├── repository/         # Repository interfaces (PainRecordRepository, AiTriageRepository)
│   └── usecase/            # Pure UseCases (AnalyzePainPointsUseCase, SavePainEntryUseCase)
└── ui/
    ├── navigation/         # Navigation Graph & Screen routes
    ├── theme/              # Color, Type, Shape, Theme
    ├── components/         # PainLogBottomSheet, VASlider, SensationFilterChips
    └── screens/
        ├── dashboard/      # Main Dashboard with quick stats & start CTA
        ├── painmap/        # 3D SceneView Pain-Mapping Screen with marker placement
        └── triage/         # AI Triage & Clinical Report Screen
```
