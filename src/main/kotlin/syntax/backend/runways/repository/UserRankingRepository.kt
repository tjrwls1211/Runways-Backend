package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Season
import syntax.backend.runways.entity.User
import syntax.backend.runways.entity.UserRanking

interface UserRankingRepository : JpaRepository<UserRanking, Long> {
    fun findByUserAndSeason(user: User, season: Season): UserRanking?
    fun findBySeasonOrderByScoreDesc(season: Season, pageable: Pageable): Page<UserRanking>
}