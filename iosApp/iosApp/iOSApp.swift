import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.doInitKoin(appDeclaration: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
