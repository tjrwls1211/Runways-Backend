package syntax.backend.runways.service

import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.User

interface RunningLogApiService {
    fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, user: User) : RunningLog
}