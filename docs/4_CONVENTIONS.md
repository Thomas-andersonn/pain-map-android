# Engineering Standards & Coding Conventions: PainMapAI
**MVVM + UDF Architecture Standards**

---

## 1. MVVM + Unidirectional Data Flow (UDF) Rules
- **State Flow Direction (Downwards):** ViewModel -> `StateFlow<UiState>` -> Screen Composable -> Child Stateless Composables.
- **Action Flow Direction (Upwards):** Child Composable -> Event Lambda -> Screen Composable -> `viewModel.onAction(action: UiAction)` -> ViewModel.
- **Single UiState:** Each screen ViewModel exposes exactly one immutable `StateFlow<UiState>`.
- **Sealed UiAction:** All user triggers on a screen are modeled in a sealed interface (e.g., `PainMapUiAction.SelectRegion`, `PainMapUiAction.SubmitPainLog`).
- **Lifecycle Collection:** Collect UI state in Composables using `val state by viewModel.uiState.collectAsStateWithLifecycle()`.

---

## 2. Jetpack Compose Guidelines
- **Stateless Child Composables:** Child composables must never receive `ViewModel` instances or repository instances. They only accept state primitives/data classes and event callbacks `(UiAction) -> Unit` or specific lambdas `() -> Unit`.
- **Recomposition Safety:** Use `@Immutable` / `@Stable` data classes. Provide explicit `key` parameters in `LazyColumn` / `LazyRow`.
- **Theme & Tokens:** Only use tokens defined in `com.example.painmap.ui.theme` (`MaterialTheme.colorScheme`, `MaterialTheme.typography`).
- **Modifiers:** Always expose an optional `modifier: Modifier = Modifier` as the first optional parameter.

---

## 3. Concurrency & Threading Invariants
- **Dispatcher Encapsulation:** ViewModels run on `Dispatchers.Main` (via `viewModelScope`). Any I/O work (network calls, Gemini SDK calls, disk caching) MUST be switched to `Dispatchers.IO` inside the Repository/Data Source implementation (`withContext(Dispatchers.IO)`).
- **Flow Safety:** Flows emitting from repositories must remain cold where appropriate, or use `stateIn` with `SharingStarted.WhileSubscribed(5_000)` in ViewModels.

---

## 4. Error Handling & Result Pattern
- Repository methods return Kotlin's `Result<T>`.
- ViewModels catch and map failure states into `UiState.error` or error flags for user-friendly UI display.

---

## 5. File & Package Naming
- Packages are all lowercase without underscores (e.g., `com.example.painmap.ui.screens.painmap`).
- Composables are PascalCase (e.g., `PainIntensitySlider`, `PainMapScreen`).
- State classes use suffix `UiState` (e.g., `PainMapUiState`), Actions use suffix `UiAction` (e.g., `PainMapUiAction`).
