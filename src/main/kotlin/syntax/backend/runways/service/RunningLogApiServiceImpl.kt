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
import syntax.backend.runways.dto.UserRunningStatsDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.User
import syntax.backend.runways.exception.NotAuthorException
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.repository.RunningLogRepository
import syntax.backend.runways.entity.RunningLogStatus
import java.time.LocalDate
import java.util.UUID


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
            endTime = requestRunningLogDTO.endTime,
            mapUrl = requestRunningLogDTO.mapUrl,
            sido = requestRunningLogDTO.sido,
            sigungu = requestRunningLogDTO.sigungu,
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

    // 러닝로그 조회
    override fun getRunningLog(startTime: LocalDate, endTime: LocalDate, userId: String, pageable: Pageable): Page<RunningLogDTO> {
        val runningLogs = runningLogRepository.findByUserIdAndStatusAndEndTimeBetweenOrderByEndTimeDesc(
            userId,
            RunningLogStatus.PUBLIC,
            startTime.atStartOfDay(),
            endTime.atTime(23, 59, 59),
            pageable
        )
        return runningLogs.map { RunningLogDTO.from(it) }
    }

    // 러닝로그 삭제
    override fun deleteRunningLog(runningLogId: UUID, userId: String) {
        val runningLog = runningLogRepository.findById(runningLogId)
            .orElseThrow { EntityNotFoundException("러닝 로그를 찾을 수 없습니다") }

        if (runningLog.user.id != userId) {
            throw NotAuthorException("러닝 로그의 작성자가 아닙니다")
        }

        runningLog.status= RunningLogStatus.DELETED
        runningLogRepository.save(runningLog)
    }

    // 유저 러닝 통계 조회
    override fun getRunningStats(userId: String): UserRunningStatsDTO {
        val totalDistance = runningLogRepository.sumDistanceByUserIdAndStatus(userId, RunningLogStatus.PUBLIC) ?: 0.0
        val totalDuration = runningLogRepository.sumDurationByUserIdAndStatus(userId, RunningLogStatus.PUBLIC) ?: 0L
        val totalLogs = runningLogRepository.countByUserIdAndStatus(userId, RunningLogStatus.PUBLIC)
        val maxSpeed = runningLogRepository.findMaxSpeedByUserIdAndStatus(userId, RunningLogStatus.PUBLIC) ?: 0.0f

        // 평균 거리, 시간 계산
        val averageDistance = if (totalLogs > 0) totalDistance / totalLogs else 0.0
        val averageDuration = if (totalLogs > 0) totalDuration.toDouble() / totalLogs else 0.0

        // 평균 페이스 (duration: 초 → 분, distance: km)
        val averagePace = if (totalDistance > 0) (totalDuration / 60.0) / totalDistance else 0.0  // 단위: 분/km

        return UserRunningStatsDTO(
            totalRunningDistance = totalDistance,
            totalRunningTime = totalDuration,
            totalWorkoutCount = totalLogs.toInt(),
            averageDistance = String.format("%.2f", averageDistance).toDouble(),
            averageDuration = String.format("%.2f", averageDuration).toDouble(),
            averagePace = String.format("%.2f", averagePace).toDouble(),
            maxSpeed = maxSpeed
        )
    }

}