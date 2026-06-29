import KakaoMapsSDK // Kakao 지도 SDK의 지도 컨트롤러, 지도 뷰, 카메라 API를 사용합니다.
import SwiftUI // SwiftUI 화면에서 UIKit 기반 지도 컨트롤러를 감싸기 위해 사용합니다.
import UIKit // UIViewController와 UIKit 뷰 계층을 구성하기 위해 사용합니다.
import CoreLocation // iOS 위치 권한 요청과 현재 위치 조회를 위해 사용합니다.

struct KakaoMapNativeView: UIViewControllerRepresentable { // SwiftUI에서 UIKit UIViewController를 사용할 수 있게 감싸는 래퍼입니다.
    func makeUIViewController(context: Context) -> KakaoMapViewController { // SwiftUI가 최초로 표시할 UIKit 컨트롤러를 생성합니다.
        KakaoMapViewController() // 실제 KakaoMap 엔진과 위치 권한 처리를 담당하는 컨트롤러를 반환합니다.
    }

    func updateUIViewController(_ uiViewController: KakaoMapViewController, context: Context) {} // SwiftUI 상태 갱신 시 호출되지만 현재는 별도 갱신 로직이 없습니다.
}

final class KakaoMapViewController: UIViewController, MapControllerDelegate, CLLocationManagerDelegate { // KakaoMap 생명주기와 CoreLocation 콜백을 함께 처리하는 화면 컨트롤러입니다.
    private var mapContainer: KMViewContainer? // KakaoMap이 실제로 렌더링될 UIKit 컨테이너 뷰를 보관합니다.
    private var mapController: KMController? // KakaoMap 엔진 준비, 활성화, 지도 뷰 추가를 담당하는 컨트롤러를 보관합니다.
    private var pendingCoordinate: CLLocationCoordinate2D? // 지도 뷰 준비 전에 위치가 먼저 도착했을 때 나중에 이동하기 위해 좌표를 임시 저장합니다.

    private let locationManager = CLLocationManager() // iOS 위치 권한 상태 확인과 단발 위치 요청을 담당합니다.
    private let mapViewName = "ramap" // KakaoMap SDK에 등록한 지도 viewName을 한 곳에서 관리합니다.

    override func viewDidLoad() { // UIViewController의 뷰가 메모리에 로드된 직후 호출됩니다.
        super.viewDidLoad() // 상위 UIViewController의 기본 초기화 동작을 먼저 수행합니다.

        locationManager.delegate = self // 위치 권한 변경, 위치 수신, 실패 콜백을 이 컨트롤러에서 받도록 연결합니다.
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters // 주변 지도 이동 목적에 맞춰 약 100m 수준의 정확도를 요청합니다.

        let container = KMViewContainer(frame: view.bounds) // 현재 화면 크기와 같은 KakaoMap 렌더링 컨테이너를 생성합니다.
        container.autoresizingMask = [.flexibleWidth, .flexibleHeight] // 화면 크기가 바뀌어도 컨테이너가 함께 늘어나도록 설정합니다.
        view.addSubview(container) // 생성한 KakaoMap 컨테이너를 현재 UIViewController의 뷰 계층에 추가합니다.

        let controller = KMController(viewContainer: container) // KakaoMap 엔진을 제어할 KMController를 컨테이너와 연결해 생성합니다.
        controller.delegate = self // 엔진 준비 후 addViews, 인증 실패 같은 KakaoMap 콜백을 이 컨트롤러에서 받습니다.
        controller.prepareEngine() // KakaoMap 엔진을 준비하고, 준비가 끝나면 delegate의 addViews가 호출됩니다.

        mapContainer = container // 컨테이너가 해제되지 않도록 프로퍼티에 보관합니다.
        mapController = controller // 지도 엔진 제어를 이후 생명주기와 카메라 이동에서 사용할 수 있도록 보관합니다.
    }

    override func viewWillAppear(_ animated: Bool) { // 화면이 사용자에게 보이기 직전에 호출됩니다.
        super.viewWillAppear(animated) // 상위 UIViewController의 표시 준비 동작을 먼저 수행합니다.
        mapController?.activateEngine() // 화면이 보일 때 KakaoMap 렌더링 엔진을 활성화합니다.
    }

    override func viewWillDisappear(_ animated: Bool) { // 화면이 사라지기 직전에 호출됩니다.
        mapController?.pauseEngine() // 화면이 보이지 않을 때 KakaoMap 엔진을 일시 정지해 리소스 사용을 줄입니다.
        super.viewWillDisappear(animated) // 상위 UIViewController의 사라짐 처리 동작을 이어서 수행합니다.
    }

    deinit { // KakaoMapViewController가 메모리에서 해제될 때 호출됩니다.
        mapController?.pauseEngine() // 해제 전에 KakaoMap 엔진을 먼저 일시 정지합니다.
        mapController?.resetEngine() // KakaoMap 엔진 상태를 초기화해 관련 리소스를 정리합니다.
        mapController?.delegate = nil // 해제된 컨트롤러로 delegate 콜백이 오지 않도록 연결을 끊습니다.
    }

    func addViews() { // KakaoMap 엔진 준비가 끝난 뒤 SDK가 호출하는 지도 뷰 추가 콜백입니다.
        let position = MapPoint(longitude: 127.108621, latitude: 37.402005) // 위치 권한이 없거나 위치 조회 전일 때 사용할 기본 지도 중심 좌표입니다.
        let viewInfo = MapviewInfo( // KakaoMap SDK에 추가할 지도 뷰 설정 정보를 생성합니다.
            viewName: mapViewName, // 나중에 getView로 다시 찾을 수 있도록 지도 뷰 이름을 지정합니다.
            appName: "openmap", // KakaoMap SDK에서 사용할 앱/맵 구성 이름입니다.
            viewInfoName: "map", // KakaoMap SDK 리소스 설정에서 사용할 지도 viewInfo 이름입니다.
            defaultPosition: position, // 지도 최초 표시 시 사용할 기본 중심 좌표입니다.
            defaultLevel: 15, // 지도 최초 표시 시 사용할 기본 줌 레벨입니다.
            enabled: true // 지도 뷰를 생성 직후 활성화 상태로 추가합니다.
        )
        mapController?.addView(viewInfo) // 준비된 설정으로 KakaoMap 지도 뷰를 컨트롤러에 추가합니다.

        requestLocationPermissionIfNeeded() // 지도 뷰가 추가된 뒤 위치 권한 상태를 확인하고 필요한 위치 동작을 시작합니다.

        if let pendingCoordinate { // 지도 뷰가 준비되기 전에 저장해둔 위치 좌표가 있는지 확인합니다.
            moveMap(to: pendingCoordinate) // 저장된 좌표가 있으면 지도 준비 후 해당 위치로 카메라를 이동합니다.
            self.pendingCoordinate = nil // 사용한 임시 좌표를 비워 중복 이동을 방지합니다.
        }
    }

    func authenticationFailed(_ errorCode: Int, desc: String) { // KakaoMap SDK 인증이 실패했을 때 호출되는 콜백입니다.
        print("KakaoMapsSDK authentication failed: \(errorCode), \(desc)") // 인증 실패 원인을 디버깅할 수 있도록 로그를 남깁니다.
    }

    private func requestLocationPermissionIfNeeded() { // 현재 iOS 위치 권한 상태에 따라 요청 또는 위치 이동을 분기합니다.
        switch locationManager.authorizationStatus { // CLLocationManager가 알고 있는 현재 위치 권한 상태를 확인합니다.
        case .notDetermined: // 사용자가 아직 위치 권한을 허용하거나 거부하지 않은 상태입니다.
            locationManager.requestWhenInUseAuthorization() // 앱 사용 중 위치 권한 팝업을 표시합니다.

        case .authorizedWhenInUse, .authorizedAlways: // 앱 사용 중 또는 항상 위치 권한이 허용된 상태입니다.
            moveToLastKnownLocation() // 권한이 있으므로 마지막 위치 또는 단발 위치 요청으로 지도 이동을 시도합니다.

        case .denied, .restricted: // 사용자가 거부했거나 시스템 정책으로 위치 사용이 제한된 상태입니다.
            break // 권한 요청을 반복하지 않고 기본 지도 위치를 그대로 유지합니다.

        @unknown default: // 이후 iOS 버전에서 새로운 권한 상태가 추가될 가능성에 대비합니다.
            break // 알 수 없는 상태에서는 안전하게 아무 동작도 하지 않습니다.
        }
    }

    private func moveToLastKnownLocation() { // 마지막으로 알려진 위치를 사용하거나, 없으면 현재 위치를 한 번 요청합니다.
        if let coordinate = locationManager.location?.coordinate { // CLLocationManager가 이미 알고 있는 마지막 좌표가 있는지 확인합니다.
            moveMap(to: coordinate) // 마지막 좌표가 있으면 즉시 해당 위치로 지도 카메라를 이동합니다.
        } else { // 마지막 위치가 아직 없는 경우입니다.
            locationManager.requestLocation() // 현재 위치를 단발로 요청하고 결과는 delegate 콜백으로 받습니다.
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) { // iOS 14 이상에서 위치 권한 상태가 변경되면 호출됩니다.
        requestLocationPermissionIfNeeded() // 변경된 권한 상태를 다시 확인해 허용 시 위치 이동으로 이어갑니다.
    }

    func locationManager( // iOS 13에서 위치 권한 변경 콜백을 받기 위한 메서드입니다.
        _ manager: CLLocationManager, // 권한 상태가 변경된 CLLocationManager 인스턴스입니다.
        didChangeAuthorization status: CLAuthorizationStatus // 변경된 위치 권한 상태 값입니다.
    ) {
        requestLocationPermissionIfNeeded() // 변경된 권한 상태를 다시 확인하고 필요한 위치 동작을 이어갑니다.
    }

    func locationManager( // requestLocation으로 단발 위치 요청에 성공했을 때 호출되는 메서드입니다.
        _ manager: CLLocationManager, // 위치 요청을 수행한 CLLocationManager 인스턴스입니다.
        didUpdateLocations locations: [CLLocation] // iOS가 전달한 위치 목록이며 보통 마지막 값이 가장 최신 위치입니다.
    ) {
        guard let coordinate = locations.last?.coordinate else { return } // 최신 위치 좌표가 없으면 지도 이동을 중단합니다.

        moveMap(to: coordinate) // 수신한 최신 좌표로 KakaoMap 카메라를 이동합니다.
    }

    func locationManager( // 단발 위치 요청이 실패했을 때 호출되는 메서드입니다.
        _ manager: CLLocationManager, // 위치 요청을 수행한 CLLocationManager 인스턴스입니다.
        didFailWithError error: Error // 위치 요청 실패 원인을 담고 있는 에러입니다.
    ) {
        print("Failed to request location: \(error.localizedDescription)") // 위치 실패 시 앱은 유지하고 디버깅용 로그만 남깁니다.
    }

    private func moveMap(to coordinate: CLLocationCoordinate2D) { // CoreLocation 좌표를 KakaoMap 좌표로 바꿔 지도 카메라를 이동합니다.
        let position = MapPoint( // KakaoMap SDK가 사용하는 지도 좌표 타입을 생성합니다.
            longitude: coordinate.longitude, // CoreLocation 좌표의 경도를 KakaoMap MapPoint 경도로 전달합니다.
            latitude: coordinate.latitude // CoreLocation 좌표의 위도를 KakaoMap MapPoint 위도로 전달합니다.
        )

        guard let mapView = mapController?.getView(mapViewName) as? KakaoMap else { // 등록한 이름의 KakaoMap 뷰를 가져올 수 있는지 확인합니다.
            pendingCoordinate = coordinate // 아직 지도 뷰가 준비되지 않았다면 좌표를 임시 저장합니다.
            return // 지도 뷰가 없으므로 이번 카메라 이동은 중단합니다.
        }

        let cameraUpdate = CameraUpdate.make( // 지정한 좌표와 줌 레벨로 이동하기 위한 KakaoMap 카메라 업데이트를 생성합니다.
            target: position, zoomLevel: 15, mapView: mapView // 이동 대상 좌표, 줌 레벨, 적용할 지도 뷰를 지정합니다.
        )

        mapView.moveCamera(cameraUpdate) // 생성한 카메라 업데이트를 지도에 적용해 현재 위치로 이동합니다.
    }
}
