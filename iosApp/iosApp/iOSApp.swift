import SwiftUI
import Shared
import KakaoMapsSDK

@main
struct iOSApp: App {
    init() {
        SDKInitializer.InitSDK(appKey: RamapAppConfig.shared.kakaoNativeAppKey)
        KoinInitializerKt.doInitKoin(appDeclaration: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
