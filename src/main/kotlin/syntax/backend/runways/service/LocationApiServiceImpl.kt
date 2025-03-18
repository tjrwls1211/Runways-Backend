package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Location
import syntax.backend.runways.repository.LocationRepository
import kotlin.math.*;

@Service
class LocationApiServiceImpl(private val locationRepository: LocationRepository) : LocationApiService {

    // 가장 가까운 관측소 찾기
    override fun getNearestLocation(x: Double, y: Double): Location? {
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
    private fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return abs((x1 - x2)) + abs((y1 - y2))
    }
}