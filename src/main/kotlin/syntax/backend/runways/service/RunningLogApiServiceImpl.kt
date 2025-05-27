package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.*
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

    override fun getRunningStats(userId: String, nowDate: LocalDate): RunningStatsResponseDTO {
        // 1일부터 nowDate까지의 날짜 범위 설정
        val startOfMonth = nowDate.withDayOfMonth(1)
        val endOfMonth = nowDate.withDayOfMonth(nowDate.lengthOfMonth())

        val totalDistance = runningLogRepository.sumDistanceByUserIdAndStatus(userId, RunningLogStatus.PUBLIC) ?: 0.0
        val totalDuration = runningLogRepository.sumDurationByUserIdAndStatus(userId, RunningLogStatus.PUBLIC) ?: 0L
        val totalLogs = runningLogRepository.countByUserIdAndStatus(userId, RunningLogStatus.PUBLIC)

        // 평균 거리, 시간 계산
        val averageDistance = if (totalLogs > 0) totalDistance / totalLogs else 0.0
        val averageDuration = if (totalLogs > 0) totalDuration.toDouble() / totalLogs else 0.0

        // 1일부터 nowDate까지의 날짜 리스트 생성
        val dateRange = (0..<nowDate.dayOfMonth).map { startOfMonth.plusDays(it.toLong()) }

        // 데이터베이스에서 가져온 dailyCounts를 Map으로 변환 (날짜 -> 횟수)
        val dailyCountsMap = runningLogRepository.findDailyCountsByUserIdAndStatusAndDateBetween(
            userId,
            RunningLogStatus.PUBLIC,
            startOfMonth.atStartOfDay(),
            nowDate.atTime(23, 59, 59)
        ).associate {
            val date = (it[0] as java.sql.Date).toLocalDate()
            val count = (it[1] as Long).toInt()
            date to count
        }

        // 날짜 범위에 따라 러닝 횟수 리스트 생성 (없으면 0으로 채움)
        val dailyCounts = dateRange.map { dailyCountsMap[it] ?: 0 }

        val monthlyStats = MonthlyRunningStatsDTO(
            averageDistance = String.format("%.2f", averageDistance).toDouble(),
            averageDuration = String.format("%.2f", averageDuration).toDouble(),
            averageCount = if (dailyCounts.isNotEmpty()) dailyCounts.sum().toDouble() / dailyCounts.size else 0.0,
            totalDistance = totalDistance,
            totalDuration = totalDuration.toDouble(),
            dailyCounts = dailyCounts
        )

        // 전체 누적 통계
        val totalStats = TotalRunningStatsDTO(
            totalDistance = totalDistance,
            totalDuration = totalDuration.toDouble(),
            totalCount = totalLogs.toInt()
        )

        return RunningStatsResponseDTO(
            monthlyStats = monthlyStats,
            totalStats = totalStats
        )
    }

}