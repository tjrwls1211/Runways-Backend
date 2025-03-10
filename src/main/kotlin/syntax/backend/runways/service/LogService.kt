package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Log
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.LogRepository
import java.util.*

@Service
class LogService(private val logRepository: LogRepository) {

    fun saveLog(userId: User?, type: String, ip: String, value: String) {
        val log = Log(
            type = type,
            ip = ip,
            user = userId,
            value = value
        )
        logRepository.save(log)
    }
}