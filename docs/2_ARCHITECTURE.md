# System Architecture & Technical Specifications: PainMapAI

## 1. Architectural Style: Clean Architecture & UDF
PainMapAI follows **Clean Architecture** principles strictly combined with **Unidirectional Data Flow (UDF)**.

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                 │
│  - Screens (Stateless Composables + Stateful Shells)   │
│  - ViewModels (UiState StateFlow & UiAction handling)   │
└───────────────────────────▲─────────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────────┐
│                     Domain Layer (Kotlin)               │
│  - Domain Models (PainPoint, Assessment, Trend)         │
│  - Use Cases (RecordPainUseCase, AnalyzePainUseCase)    │
│  - Repository Interfaces                                │
└───────────────────────────▲─────────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────────┐
│                      Data Layer                         │
│  - Repository Implementations                           │
│  - Local Data Source (Room DB / DataStore)              │
│  - Remote Data Source (Google Gemini Generative AI SDK) │
│  - 3D Model Assets & Parser                             │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Layer Responsibilities & Package Boundaries

```
com.example.painmap/
├── data/
│   ├── local/            # Room Database, Entities, DAOs
│   ├── remote/           # Gemini AI client & prompt templates
│   ├── repository/       # Repository implementations
│   └── mapper/           # Entity <-> Domain mappers
├── domain/
│   ├── model/            # Pure Kotlin domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Single-purpose business UseCases
└── ui/
    ├── navigation/       # NavHost, Routes & Destinations
    ├── theme/            # Material3 Color, Type, Shape, Theme
    ├── components/       # Reusable design system widgets & cards
    └── screens/
        ├── dashboard/    # Main overview & quick actions
        ├── painmap/      # 3D SceneView body map & point selector
        ├── assessment/   # AI analysis chat & consultation report
        └── history/      # Timeline & trend charts
```

---

## 3. Technology Stack & Key Dependencies
- **UI Framework:** Jetpack Compose (Compose BOM `2024.06.00`)
- **Design System:** Material 3 (`androidx.compose.material3:material3`)
- **3D Graphics:** SceneView Android (`io.github.sceneview:sceneview:2.2.1`)
- **AI Intelligence:** Google Generative AI SDK (`com.google.ai.client.generativeai:generativeai:0.9.0`)
- **Navigation:** AndroidX Navigation Compose (`2.7.7`)
- **Concurrency:** Kotlin Coroutines (`1.8.1`) & Reactive Flows
- **Serialization:** Kotlinx Serialization JSON (`1.6.3`)
- **Language / Tooling:** Kotlin `2.0.0`, Android Gradle Plugin `8.4.2`, JVM `17`, MinSDK `24`, TargetSDK `34`.

---

## 4. State Management Rules
1. **Single Source of Truth:** Each screen has exactly one immutable `UiState` data class.
2. **Action Ingestion:** User interactions are dispatched via a sealed interface `UiEvent` or `UiAction` to the ViewModel.
3. **Lifecycle-Aware Observation:** In Compose, UI collects state strictly via `collectAsStateWithLifecycle()`.
4. **Stateless UI:** Child composables must never receive ViewModel references; they only accept state values and lambda callbacks.
