# Agent guide — Paintres Lumière Android

This repository is the **Android client** of Paintres Lumière, a B2B sales app for a workshop that
manufactures laser-cut pieces and sells them to resellers. It mirrors the iOS app
(`paintresLumiere-iOS`), which is the reference implementation: when a behaviour is unclear, read
the iOS code and its `.specs/features/*/spec.md` before inventing one.

Work items live as **GitHub issues** in this repository (migrated from the Trello board
https://trello.com/b/L9Bg9uhU). Each issue links to its iOS / API counterpart.

---

## Platform and language

| | Value |
|---|---|
| Language | Kotlin 2.0, JVM target 17 |
| UI | Jetpack Compose + Material 3 (dark, fixed brand palette — no dynamic color) |
| `minSdk` / `targetSdk` | 24 / 36 |
| DI | Hilt (KSP) |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Images | Coil |

---

## Architecture — MVVM with a navigation graph as the coordinator

| iOS | Android |
|---|---|
| `AppCoordinator` / feature coordinators | `AppNavHost` and one nested graph per flow (`AuthGraph`, `MainGraph`) |
| `weak var coordinator: XCoordinatorProtocol?` | screen callbacks named after the **event**: `onProductSelected(sku)`, `onLoginSucceeded()` |
| `@Observable @MainActor` ViewModel | `@HiltViewModel` + immutable `UiState` exposed as `StateFlow` |
| SwiftUI `View` with `@Bindable` | stateless `XScreen(state, onAction…)` + a thin `XRoute` that wires the ViewModel |
| Swinject assemblies | Hilt modules under `di/` |

### Rules

- **All navigation goes through the NavHost.** A composable never holds a `NavController`; it
  receives lambdas. The graph decides where an event leads.
- **ViewModels never see Android UI types** (no `Context`, no `NavController`). One-shot events
  (navigate, show a toast) go out through a `Channel`/`SharedFlow` or a callback in the route.
- **State is immutable.** Each screen has one `data class XUiState`; the ViewModel updates it with
  `_state.update { it.copy(...) }`.
- **Screens are split in two:** `XRoute` (gets the ViewModel via `hiltViewModel()`, collects state
  with `collectAsStateWithLifecycle()`) and `XScreen` (pure, previewable, testable).
- **Hilt fails loudly.** A missing binding is a compile error — never add a manual fallback
  construction "just in case" (iOS decision AD-006).

### Package layout

```
com.luxkasTech.PaintresLumiere
├── core/
│   ├── designsystem/   tokens (PLColor, PLFont, PLSpacing…) and PL* components
│   ├── network/        Retrofit API, ApiError, interceptors
│   ├── session/        secure token storage, SessionManager
│   └── messages/       toast queue (MessagesService) and ToastHost
├── data/               models and repositories, per domain (product, auth, profile…)
├── feature/<name>/     XRoute, XScreen, XViewModel, XUiState
├── navigation/         AppNavHost and the route definitions
└── di/                 Hilt modules
```

---

## API rules

- Every enum decoded from the API **must tolerate unknown values** (fallback to `UNKNOWN`) — one
  new category or color on the backend must never take down a whole list (iOS AD-005).
- The catalog returns **one row per variant**: the same SKU spans several rows by color/dimension.
- Never show a raw HTTP status to the user; map through `ApiError`.

## Strings

- **PT-BR is the base language** (`values/strings.xml`); English lives in `values-en/` (iOS AD-007).
- No hardcoded user-facing text in composables.
- String keys follow `feature_screen_element` (e.g. `auth_login_title`); drop `screen` when the
  feature has a single screen (`cart_subtotal`). Text shared across features uses `common_`.
- Every new PT-BR string needs its English translation: lint fails on `MissingTranslation`.

---

## Workflow

- Branch: `GH-{issue}/{type}/claude/{short_description}`; commit messages follow the root
  `CLAUDE.md`: a single line `{type}(GH-{issue}): {short description}` (English).
- PR descriptions in **PT-BR**, linking the issue with `Closes #N`.
- Work is delivered as **stacked PRs** with `gh stack` (see the iOS repo's
  `.claude/skills/gh-stack/SKILL.md`). Never commit to `main`.
- CI (`.github/workflows/android-ci.yml`) runs `assembleDebug`, `testDebugUnitTest` and
  `lintDebug` on every PR, including PRs based on another stacked branch.
