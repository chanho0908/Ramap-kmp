# Ramap Native MVP 구현 계획

웹 버전 Ramap(https://github.com/chanho0908/Ramap)의 핵심 가치를 네이티브 환경(KMP)으로 이식하기 위한 MVP(Minimum Viable Product) 로드맵입니다.

## 핵심 목표
1.  **위치 기반 라멘집 탐색**: 지도 상에서 주변 라멘집 확인.
2.  **데이터 무결성**: Supabase 연동을 통한 웹/앱 데이터 동기화.
3.  **네이티브 사용자 경험**: 매끄러운 지도 인터랙션 및 오프라인 우선(선택적) 고려.
4.  **익명 참여**: 로그인 없이도 가능한 리뷰 조회 및 작성.

## 1단계: 프로젝트 기반 구축 (1주차)
*   **KMP 환경 고도화**:
    *   `shared` 모듈에 Supabase Kotlin SDK (`postgrest`, `storage`, `auth`) 설정.
    *   네트워크 레이어 구성 (Ktor + Serialization).
    *   의존성 주입(Koin) 설정.
*   **공통 데이터 모델 정의**:
    *   DB Schema에 맞춘 `Shop`, `Review`, `Visit` 데이터 클래스 생성.
*   **기본 UI 구조**:
    *   Compose Multiplatform 기반 Bottom Navigation 구성 (Map, List, My).

## 2단계: 지도 및 위치 서비스 통합 (2주차)
*   **네이티브 지도 인터페이스 (Expect/Actual)**:
    *   Android: Google Maps (혹은 Kakao Map SDK) 통합.
    *   iOS: Google Maps (혹은 Apple Maps) 통합.
*   **사용자 위치 추적**:
    *   플랫폼별 권한 처리 및 현재 위치 표시.
*   **Map UI 구현**:
    *   지도를 드래그할 때 해당 영역의 라멘집 데이터 호출 로직 구현.

## 3단계: 콘텐츠 및 상세 정보 (3주차)
*   **라멘집 상세 페이지**:
    *   정보 표시 (영업시간, 주소, 평점).
    *   Supabase Storage 연동을 통한 이미지 갤러리 구현 (Coil/Kamamel 사용).
*   **리뷰 시스템**:
    *   기존 리뷰 목록 페이징 처리.
    *   익명 리뷰 작성 로직 구현.

## 4단계: 사용자 기능 및 방문 기록 (4주차)
*   **체크인 기능**:
    *   현재 위치 기반 방문 인증 및 Supabase DB 저장.
*   **방문 히스토리**:
    *   사용자별 방문 통계 및 목록 보기.
*   **검색 기능**:
    *   키워드 기반 라멘집 검색 및 지도 이동.

## 5단계: 안정화 및 배포 준비 (5주차)
*   **UI/UX 디테일 개선**:
    *   Lottie 애니메이션 (로딩 등) 적용.
    *   네이티브 햅틱 피드백.
*   **성능 최적화**:
    *   이미지 캐싱 및 지도 마커 클러스터링.
*   **QA 및 배포**:
    *   Android Play Store / iOS App Store 배포 준비.

---

## 기술 스택 제안
*   **Language**: Kotlin (100%)
*   **Framework**: Kotlin Multiplatform, Compose Multiplatform
*   **Backend**: Supabase (Existing)
*   **Networking**: Ktor
*   **Dependency Injection**: Koin
*   **Image Loading**: Coil3 (Multiplatform support)
*   **Local DB**: SQLDelight (선택 사항, 오프라인 지원 필요 시)

## 사용자 검토 필요 사항
1.  **지도 라이브러리 선택**: Android에서 웹과 동일하게 Kakao Map을 사용할지, 아니면 크로스 플랫폼 구현이 용이한 Google Maps를 사용할지 결정이 필요합니다.
2.  **로그인 여부**: 웹 버전처럼 완전 익명을 유지할지, 소셜 로그인을 최소한으로 도입할지 여부.
