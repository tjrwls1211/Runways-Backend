package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.dto.RunningLogDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.repository.RunningLogRepository


@Service
class RunningLogApiServiceImpl (
    private val runningLogRepository: RunningLogRepository,
    private val courseRepository: CourseRepository,
    private val experienceService: ExperienceService,
) : RunningLogApiService {

    private val wktReader = WKTReader()

    // 러닝로그 저장
    @Transactional
    override fun saveRunningLog(requestRunningLogDTO: RequestRunningLogDTO, user : User): RunningLog {
        val course = requestRunningLogDTO.courseId?.let {
            courseRepository.findById(it).orElseThrow { EntityNotFoundException("코스를 찾을 수 없습니다 : $it") }
        }

        // WKT 문자열을 Geometry 객체로 변환
        val position = wktReader.read(requestRunningLogDTO.position) // Point
        val coordinate = wktReader.read(requestRunningLogDTO.coordinate) // LineString

        // 유효성 검사
        if (position.geometryType != "Point" || coordinate.geometryType != "LineString") {
            throw IllegalArgumentException("유효하지 않은 WKT 형식: position은 Point여야 하고 coordinate는 LineString이어야 합니다.")
        }

        // 러닝로그 생성
        val runningLog = RunningLog(
            user = user,
            course = course,
            distance = requestRunningLogDTO.distance,
            duration = requestRunningLogDTO.duration,
            avgSpeed = requestRunningLogDTO.avgSpeed,
            maxSpeed = requestRunningLogDTO.maxSpeed,
            position = position as Point,
            coordinate = coordinate as LineString,
            startTime = requestRunningLogDTO.startTime,
            endTime = requestRunningLogDTO.endTime
        )

        // 코스가 있을 경우 사용 횟수 증가
        course?.let {
            it.usageCount += 1
            courseRepository.save(it)
        }

        // 경험치 추가
        experienceService.addExperience(user, 30)

        // 러닝로그 저장
        return runningLogRepository.save(runningLog)
    }

    override fun getRunningLog(userId: String, pageable: Pageable): Page<RunningLogDTO> {
        val runningLogs = runningLogRepository.findByUserIdOrderByEndTimeDesc(userId, pageable)
        return runningLogs.map { RunningLogDTO.from(it) }
    }
}