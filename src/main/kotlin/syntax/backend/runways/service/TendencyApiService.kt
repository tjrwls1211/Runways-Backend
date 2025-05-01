package syntax.backend.runways.service

import syntax.backend.runways.dto.TendencyDTO

interface TendencyApiService {
    fun saveTendency(token: String, tendencyDTO: TendencyDTO)
    fun getTendency(token: String): TendencyDTO?
}