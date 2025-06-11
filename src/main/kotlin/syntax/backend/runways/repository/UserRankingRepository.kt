package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Season
import syntax.backend.runways.entity.User
import syntax.backend.runways.entity.UserRanking

@Repository
interface UserRankingRepository : JpaRepository<UserRanking, Long> {
    fun findByUserAndSeason(user: User, season: Season): UserRanking?
    fun findBySeasonOrderByScoreDesc(season: Season, pageable: Pageable): Page<UserRanking>
    fun deleteByUserId(userId: String): Int
}