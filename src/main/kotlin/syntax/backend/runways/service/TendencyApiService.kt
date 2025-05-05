package syntax.backend.runways.service

import syntax.backend.runways.dto.TendencyDTO

interface TendencyApiService {
    fun saveTendency(userId: String, tendencyDTO: TendencyDTO)
    fun getTendency(userId: String): TendencyDTO?
}