# Engineering Standards & Coding Conventions: PainMapAI

## 1. Jetpack Compose Guidelines
- **Lifecycle-Aware State Collection:** Always observe StateFlow using `collectAsStateWithLifecycle()` in root screen composables (`androidx.lifecycle.compose`).
- **Stateless Child Composables:** Pass explicit data models and lambda callbacks. Never pass `ViewModel` instances into child composables.
- **Recomposition Optimization:** Use `@Immutable` or `@Stable` annotations on UI state data classes where necessary. Keys must be specified for all `LazyColumn` / `LazyRow` items.
- **Previews:** Every stateless component must have a `@Preview` showing light and dark theme variations.
- **Modifiers:** Always accept an optional `modifier: Modifier = Modifier` as the first optional parameter.

---

## 2. Architecture & Unidirectional Data Flow (UDF)
- **Single UiState:** Each screen ViewModel exposes a single `StateFlow<UiState>`.
- **Sealed UiAction / UiEvent:** User interactions are modeled as sealed interfaces and dispatched via a single `onAction(action: UiAction)` function on the ViewModel.
- **No Direct Repository Access:** UI must strictly interact through ViewModels and Domain UseCases.

---

## 3. Concurrency & Threading Invariants
- **Dispatcher Encapsulation:** ViewModels run on `Dispatchers.Main` via `viewModelScope`. All I/O work (Room DB queries, network calls, file reading) must switch to `Dispatchers.IO` inside the Repository / Data Source implementation (`withContext(Dispatchers.IO)`).
- **Flow Safety:** Flows emitting from repositories must remain cold where appropriate, or use `stateIn` with `SharingStarted.WhileSubscribed(5_000)` in ViewModels.

---

## 4. Error Handling & Result Pattern
- Domain and Data layers return Kotlin's `Result<T>` or a sealed `DataError` / `NetworkError` type.
- Never catch generic `Throwable` silently. Convert exceptions into structured UI error states (e.g. `UiState.Error(val message: String)`).

---

## 5. File & Package Naming
- Packages are all lowercase without underscores (e.g., `com.example.painmap.domain.usecase`).
- Composables are PascalCase and named descriptively (e.g., `PainIntensitySlider`, `AnatomicalModelViewer`).
- State classes use suffix `UiState` (e.g., `PainMapUiState`), Actions use suffix `UiAction` (e.g., `PainMapUiAction`).
