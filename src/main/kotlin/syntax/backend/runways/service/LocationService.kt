package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Location
import syntax.backend.runways.repository.LocationRepository
import kotlin.math.pow
import kotlin.math.sqrt

@Service
class LocationService(private val locationRepository: LocationRepository) {

    // 가장 가까운 관측소 찾기
    fun getNearestLocation(x: Int, y: Int): Location? {
        val locations = locationRepository.findAll()
        var nearestLocation: Location? = null
        var minDistance = Double.MAX_VALUE

        // 관측소 거리 비교
        for (location in locations) {
            val distance = calculateDistance(location.x, location.y, x, y)
            if (distance < minDistance) {
                minDistance = distance
                nearestLocation = location
            }
        }

        return nearestLocation
    }

    // 좌표와 관측소 거리 계산
    private fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        return sqrt((x1 - x2).toDouble().pow(2) + (y1 - y2).toDouble().pow(2))
    }
}