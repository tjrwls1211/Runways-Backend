package syntax.backend.runways.service

import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.entity.RunningLog

interface RunningLogApiService {
    fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, userId: String) : RunningLog
}