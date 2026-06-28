# KMP 테스트 환경 설정 및 샘플 테스트 구현 계획

KMP 프로젝트에서 단위 테스트(Repository/Domain)와 Compose UI 테스트를 수행하기 위한 환경을 구축하고 샘플 코드를 작성합니다.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/gradle/libs.versions.toml)
- 테스트 라이브러리 버전 추가: `kotlinx-coroutines-test`, `turbine`
- 테스트 라이브러리 정의 추가: `kotlinx-coroutines-test`, `turbine`, `compose-ui-test`

#### [MODIFY] [build.gradle.kts (shared)](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/shared/build.gradle.kts)
- `commonTest` 소스셋에 위 라이브러리들 의존성 추가

---

### Sample Implementation & Tests

#### [NEW] [SampleRepository.kt](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/shared/src/commonMain/kotlin/com/peto/ramap/data/SampleRepository.kt)
- Flow를 반환하는 간단한 Repository 예시

#### [NEW] [SampleRepositoryTest.kt](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/shared/src/commonTest/kotlin/com/peto/ramap/data/SampleRepositoryTest.kt)
- `runTest`와 `Turbine`을 사용한 Repository 단위 테스트

#### [NEW] [SampleUi.kt](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/shared/src/commonMain/kotlin/com/peto/ramap/ui/SampleUi.kt)
- 테스트용 간단한 Composable

#### [NEW] [SampleUiTest.kt](file:///Users/jeongchanho/Documents/Android/Ramap-kmp/shared/src/commonTest/kotlin/com/peto/ramap/ui/SampleUiTest.kt)
- `runComposeUiTest`를 사용한 공통 UI 테스트

## Verification Plan

### Automated Tests
- `./gradlew :shared:allTests` 명령어를 통해 공통 로직 및 UI 테스트 검증 (로컬 환경에서 실행 가능한 범위 내)
