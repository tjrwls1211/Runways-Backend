package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Season
import java.time.LocalDate

@Repository
interface SeasonRepository : JpaRepository<Season, Long> {
    fun findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(
        startDate: LocalDate,
        endDate: LocalDate,
        isActive: Boolean = true
    ): Season?
}