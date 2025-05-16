package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.dto.RunningLogDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.User


interface RunningLogApiService {
    fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, user: User) : RunningLog
    fun getRunningLog(userId: String, pageable: Pageable, ): Page<RunningLogDTO>
}