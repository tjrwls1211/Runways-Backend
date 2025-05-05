package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Season
import java.time.LocalDate

interface SeasonRepository : JpaRepository<Season, Long> {
    fun findByStartDateBeforeAndEndDateAfter(now1: LocalDate, now2: LocalDate): Season?
}