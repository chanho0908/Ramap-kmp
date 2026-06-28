import KakaoMapsSDK
import SwiftUI
import UIKit

struct KakaoMapNativeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> KakaoMapViewController {
        KakaoMapViewController()
    }

    func updateUIViewController(_ uiViewController: KakaoMapViewController, context: Context) {}
}

final class KakaoMapViewController: UIViewController, MapControllerDelegate {
    private var mapContainer: KMViewContainer?
    private var mapController: KMController?

    override func viewDidLoad() {
        super.viewDidLoad()

        let container = KMViewContainer(frame: view.bounds)
        container.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(container)

        let controller = KMController(viewContainer: container)
        controller.delegate = self
        controller.prepareEngine()

        mapContainer = container
        mapController = controller
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        mapController?.activateEngine()
    }

    override func viewWillDisappear(_ animated: Bool) {
        mapController?.pauseEngine()
        super.viewWillDisappear(animated)
    }

    deinit {
        mapController?.pauseEngine()
        mapController?.resetEngine()
        mapController?.delegate = nil
    }

    func addViews() {
        let position = MapPoint(longitude: 127.108621, latitude: 37.402005)
        let viewInfo = MapviewInfo(
            viewName: "ramap",
            appName: "openmap",
            viewInfoName: "map",
            defaultPosition: position,
            defaultLevel: 15,
            enabled: true
        )
        mapController?.addView(viewInfo)
    }

    func authenticationFailed(_ errorCode: Int, desc: String) {
        print("KakaoMapsSDK authentication failed: \(errorCode), \(desc)")
    }
}
