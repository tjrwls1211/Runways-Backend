package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.UserRankingDTO
import syntax.backend.runways.entity.Season
import syntax.backend.runways.repository.SeasonRepository
import syntax.backend.runways.repository.UserRankingRepository
import java.time.LocalDate

@Service
class UserRankingApiServiceImpl(
    private val seasonRepository: SeasonRepository,
    private val userRankingRepository: UserRankingRepository
): UserRankingService {

    // 시즌 랭킹 조회
    @Transactional
    override fun getSeasonRanking(pageable: Pageable): Page<UserRankingDTO> {
       val currentSeason = findCurrentSeason() ?: throw EntityNotFoundException("현재 시즌이 존재하지 않습니다.")

        val rankingPage = userRankingRepository.findBySeasonOrderByScoreDesc(currentSeason, pageable)

        return rankingPage.map { ranking ->
            val user = ranking.user
            UserRankingDTO(
                id = user.id,
                profileImage = if (user.accountPrivate) null else user.profileImageUrl,
                nickname = if (user.accountPrivate) "비공개" else user.nickname,
                experience = ranking.score
            )
        }
    }

    private fun findCurrentSeason(now: LocalDate = LocalDate.now()): Season? {
        return seasonRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(
            startDate = now,
            endDate = now,
            isActive = true
        )
    }
}