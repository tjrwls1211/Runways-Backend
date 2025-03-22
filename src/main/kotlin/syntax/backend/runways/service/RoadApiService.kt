package syntax.backend.runways.service

import syntax.backend.runways.dto.RoadDataDTO

interface RoadApiService {
    fun getRoadDataById(id: Long): RoadDataDTO
}