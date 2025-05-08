package syntax.backend.runways.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import syntax.backend.runways.entity.PopularCourse
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.repository.PopularCourseRepository
import syntax.backend.runways.repository.RunningLogRepository
import syntax.backend.runways.entity.CourseStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class PopularCourseScheduler(
    private val runningLogRepository: RunningLogRepository,
    private val courseRepository: CourseRepository,
    private val popularCourseRepository: PopularCourseRepository
) {

    @Scheduled(cron = "0 0 * * * *") // 매 시간마다 실행
    fun updateRisingCourses() {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:30 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 저장할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        val endTime = LocalDateTime.now()
        val startTime = endTime.minusHours(1)

        // RunningLog에서 코스별 이용 횟수 집계 (course가 null인 경우 제외)
        val runningLogs = runningLogRepository.findByEndTimeBetween(startTime, endTime)
            .filter { it.course != null }

        if (runningLogs.isEmpty()) {
            println("RunningLog 데이터가 없습니다. startTime: $startTime, endTime: $endTime")
            return
        }

        val courseIdCountMap = runningLogs.groupingBy { it.course!!.id }.eachCount()
        if (courseIdCountMap.isEmpty()) {
            println("courseIdCountMap이 비어 있습니다.")
            return
        }

        // PUBLIC 상태의 코스만 필터링
        val courseIds = courseIdCountMap.keys.toList()
        if (courseIds.isEmpty()) {
            println("courseIds가 비어 있습니다.")
            return
        }

        val courses = courseRepository.findCoursesWithTagsByIdsAndStatus(courseIds, CourseStatus.PUBLIC)
        if (courses.isEmpty()) {
            println("PUBLIC 상태의 코스가 없습니다. courseIds: $courseIds")
            return
        }

        // 코스별 PopularCourse 엔티티 업데이트 또는 생성
        courses.forEach { course ->
            val usageCount = courseIdCountMap[course.id] ?: 0

            // 기존 데이터 조회
            val existingCourse = popularCourseRepository.findByDateAndCourseId(targetDate, course.id)

            if (existingCourse != null) {
                // 기존 데이터가 있으면 업데이트
                val updatedCourse = existingCourse.copy(usageCount = existingCourse.usageCount + usageCount)
                popularCourseRepository.save(updatedCourse)
            } else {
                // 기존 데이터가 없으면 새로 생성
                val newCourse = PopularCourse(
                    date = targetDate,
                    courseId = course.id,
                    usageCount = usageCount
                )
                popularCourseRepository.save(newCourse)
            }
        }

        println("급상승 코스 스케줄러 실행 종료: ${LocalDateTime.now()}")
    }
}