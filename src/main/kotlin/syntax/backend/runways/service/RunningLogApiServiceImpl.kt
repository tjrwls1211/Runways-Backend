package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.repository.RunningLogRepository
import syntax.backend.runways.repository.UserRepository

@Service
class RunningLogApiServiceImpl (
    private val runningLogRepository: RunningLogRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val experienceService: ExperienceService
) : RunningLogApiService {

    // 러닝로그 저장
    @Transactional
    override fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, userId: String): RunningLog {
        val user = userRepository.findById(userId).orElseThrow { EntityNotFoundException("사용자를 찾을 수 없습니다. : $userId") }
        val course = requestRunningLogDTO.courseId?.let {
            courseRepository.findById(it).orElseThrow { EntityNotFoundException("코스를 찾을 수 없습니다 : $it") }
        }

        val runningLog = RunningLog(
            user = user,
            course = course,
            distance = requestRunningLogDTO.distance,
            duration = requestRunningLogDTO.duration,
            speed = requestRunningLogDTO.speed,
            startTime = requestRunningLogDTO.startTime,
            endTime = requestRunningLogDTO.endTime
        )

        // 경험치 추가
        experienceService.addExperience(user, 30)

        // 러닝로그 저장
        return runningLogRepository.save(runningLog)
    }
}