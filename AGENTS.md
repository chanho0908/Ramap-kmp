# Repository Guidelines

## Project Structure & Module Organization

This is a Kotlin Multiplatform project targeting Android and iOS. The Gradle modules are `:androidApp` and `:shared`.

- `shared/src/commonMain/kotlin/com/peto/ramap`: shared app code, Compose UI, DI, data, and network code.
- `shared/src/androidMain` and `shared/src/iosMain`: platform-specific Kotlin implementations.
- `shared/src/commonTest`, `shared/src/androidHostTest`, and `shared/src/iosTest`: shared, Android host, and iOS tests.
- `shared/src/commonMain/composeResources`: Compose Multiplatform resources.
- `androidApp/src/main`: Android entry point, manifest, and resources.
- `iosApp/iosApp`: SwiftUI iOS entry point and Xcode assets.

## Build, Test, and Development Commands

- `./gradlew ktlintCheck`: run Kotlin style checks across subprojects.
- `./gradlew ktlintFormat`: format Kotlin/KTS files where possible.
- `./gradlew test`: run Gradle unit test tasks used by CI.
- `./gradlew :shared:testAndroidHostTest`: run Android host tests for the shared module.
- `./gradlew :shared:iosSimulatorArm64Test`: run iOS simulator tests.
- `./gradlew :androidApp:assembleDebug`: build the Android debug APK.

Run the iOS app from Xcode by opening `iosApp`.

## Coding Style & Naming Conventions

Use Kotlin with 4-space indentation, LF line endings, UTF-8, and final newlines as defined in `.editorconfig`. Ktlint is applied to all subprojects; generated and build directories are excluded. Compose functions may use non-standard names because `Composable` function naming is ignored. Keep package paths under `com.peto.ramap` and prefer existing patterns in `di`, `data`, `network`, and `ui`.

## Testing Guidelines

Use `kotlin.test` for common tests, with coroutine test utilities, Turbine, Compose UI test APIs, and Robolectric where needed. Place shared logic tests in `commonTest`; platform-sensitive Android behavior belongs in `androidHostTest`; iOS-specific behavior belongs in `iosTest`. Name tests after the subject and behavior, for example `SampleRepositoryTest`.

## Commit & Pull Request Guidelines

Commit messages follow `<type>: <subject>` and are normally written in Korean. Common types include `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `design`, `rename`, and `remove`.

PR titles should summarize the work in Korean without `feat:` or `fix:` prefixes. Fill out `.github/PULL_REQUEST_TEMPLATE.md`, including overview, change list, test results, related issues, and screenshots or recordings for UI changes.

## Security & Configuration Tips

Supabase values are injected through BuildKonfig from `SUPABASE_URL` and `SUPABASE_ANON_KEY`, or from `local.properties` keys `supabase.url` and `supabase.anon_key`. Do not commit secrets or machine-specific local configuration.
