package syntax.backend.runways.service

import syntax.backend.runways.entity.Location

interface LocationApiService {
    fun getNearestLocation(x: Double, y: Double): Location?
}