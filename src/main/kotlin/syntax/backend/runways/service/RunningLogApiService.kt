package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.dto.RunningLogDTO
import syntax.backend.runways.dto.RunningStatsResponseDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.User
import java.time.LocalDate
import java.util.UUID


interface RunningLogApiService {
    fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, user: User) : RunningLog
    fun getRunningLog(startTime: LocalDate, endTime: LocalDate, userId: String, pageable: Pageable): Page<RunningLogDTO>
    fun deleteRunningLog(runningLogId: UUID, userId: String)
    fun getRunningStats(userId: String, requestDate : LocalDate): RunningStatsResponseDTO
}