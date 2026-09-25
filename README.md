# MathOnGo – Question Attempt Screen

An Android app that goes through the JEE Advanced questions in the bundled `data.json` (all 599, in file order). Single-correct questions are the required part. Multiple-correct and numerical questions are done as bonus.

## Build and run

```bash
./gradlew :app:installDebug        # build and install on a connected device or emulator
./gradlew :app:testDebugUnitTest   # unit tests
```

Or open the project in Android Studio and run the `app` configuration.

## Requirements

- Android Studio Narwhal (2025.2) or newer
- AGP 9.3.2, Gradle 9.5 (wrapper included)
- JDK 17+ to launch Gradle. The daemon uses JDK 21, which Gradle downloads if it's missing.
- Android SDK Platform 36 (compileSdk 36.1). minSdk 24, targetSdk 36.

## Project structure and state

```
data/      JSON DTOs, mapper, content preparation, JsonQuestionBankRepository
domain/    Models, the repository interface, AnswerChecker (answer rules)
ui/        Attempt screen: action, state, reducer, ViewModel, components, WebView rendering
```

`domain` has no Android dependencies. The ViewModel only talks to the `QuestionBankRepository` interface, which `data` implements.

The screen follows MVI. Every user input is an `AttemptAction`. `AttemptReducer`, a pure function, turns the current state and the action into a new `AttemptUiState` (`Loading`, `Error` or `Ready`). The ViewModel exposes that state as a `StateFlow`. It also saves the current question and the answers to `SavedStateHandle`, so they survive rotation and process death.

## Rendering (MathJax, HTML, images)

- Each field is classified when the data is loaded. Plain text is drawn with a normal Compose `Text`. Anything with HTML, LaTeX, MathML or an image goes to a WebView.
- The WebView loads a local page with MathJax 3 (SVG output) bundled in `assets/`, so no CDN or network is needed for math. The page is served with `WebViewAssetLoader`, and remote images load normally.
- There is one WebView per position on screen (the question, then each option). They are reused from question to question, so MathJax is loaded only once for each.
- The page reports its content height back to Compose, and each WebView is sized to fit. Tables and formulas wider than the screen are scaled down to fit. Images that fail to load show a placeholder.
- All controls (option cards, buttons, the numeric field) are native Compose. The WebView only draws content.

## Libraries

- Jetpack Compose (Material 3) and Activity Compose
- AndroidX Lifecycle (ViewModel, `SavedStateHandle`, `collectAsStateWithLifecycle`)
- AndroidX WebKit (`WebViewAssetLoader`)
- kotlinx.serialization for JSON
- MathJax 3.2.2 (bundled asset)
- JUnit and kotlinx-coroutines-test for tests

## Known limitations

- Each rich field gets its own WebView, so a question with 4 rich options uses 5. They share one renderer process and are reused across questions, but on low-RAM devices this still adds memory pressure. At production scale I'd move to one WebView per screen that renders all the content.
- Tables and formulas wider than the screen are scaled down to fit instead of scrolling sideways. The WebViews pass touches to the native screen so cards stay tappable, which makes horizontal scrolling inside them tricky. Very wide content can end up small (down to about 0.44× in this data).
- Video solutions are only indicated, not playable.

## Video Link
https://drive.google.com/file/d/1ezPlkc6XgiNOhjccQZyc2HUUz5ipTlk6/view?usp=sharing

## APK Link
https://drive.google.com/file/d/11mIaLODeRuMMnjlbVeV0Kr2B6DENc4Qx/view?usp=sharing
