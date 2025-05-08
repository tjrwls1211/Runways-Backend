package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.UserRankingDTO

interface UserRankingService {
    fun getSeasonRanking(pageable: Pageable): Page<UserRankingDTO>
}