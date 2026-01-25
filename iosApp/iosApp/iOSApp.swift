import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.initKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
