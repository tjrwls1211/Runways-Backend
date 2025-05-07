package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Role
import syntax.backend.runways.entity.Season
import syntax.backend.runways.entity.User
import syntax.backend.runways.entity.UserRanking
import syntax.backend.runways.repository.SeasonRepository
import syntax.backend.runways.repository.UserRankingRepository
import syntax.backend.runways.repository.UserRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ExperienceServiceImpl(
    private val seasonRepository: SeasonRepository,
    private val userRepository: UserRepository,
    private val userRankingRepository: UserRankingRepository
) : ExperienceService {

    override fun addExperience(user: User, experience: Int) {
        val now = LocalDate.now()

        val currentSeason = findCurrentSeason(now)
            ?: throw EntityNotFoundException("최근 시즌이 존재하지 않습니다.")

        // USER 권한을 가진 유저만 경험치를 추가하도록 제한
        if (user.role == Role.ROLE_GUEST || user.role == Role.ROLE_ADMIN) {
            return
        }

        user.experience += experience
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)

        // 시즌 랭킹에 경험치 추가
        val ranking = userRankingRepository
            .findByUserAndSeason(user, currentSeason)
            ?: userRankingRepository.save(
                UserRanking(user = user, season = currentSeason)
            )

        ranking.score += experience
        ranking.updatedAt = LocalDateTime.now()
        userRankingRepository.save(ranking)
    }

    private fun findCurrentSeason(now: LocalDate = LocalDate.now()): Season? {
        return seasonRepository.findByStartDateBeforeAndEndDateAfter(now, now)
    }
}