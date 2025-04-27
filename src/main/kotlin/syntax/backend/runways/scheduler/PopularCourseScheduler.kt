package syntax.backend.runways.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import syntax.backend.runways.entity.PopularCourse
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.repository.CourseApiRepository
import syntax.backend.runways.repository.PopularCourseRepository
import syntax.backend.runways.repository.RunningLogApiRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class PopularCourseScheduler(
    private val runningLogApiRepository: RunningLogApiRepository,
    private val courseApiRepository: CourseApiRepository,
    private val popularCourseRepository: PopularCourseRepository
) {

    @Scheduled(cron = "0 30 04 * * *") // 매일 4시 30분 실행
    fun savePopularCourses() {
        println("스케줄러 실행 시작: ${LocalDateTime.now()}") // 스케줄러 시작 로그

        val endTime = LocalDateTime.now().withHour(4).withMinute(30).withSecond(0).withNano(0)
        val startTime = endTime.minusDays(1).withHour(4).withMinute(30).withSecond(0).withNano(0)

        // RunningLog에서 코스별 이용 횟수 집계
        val runningLogs = runningLogApiRepository.findByEndTimeBetween(startTime, endTime)

        val courseIdCountMap = runningLogs.groupingBy { it.course.id }.eachCount()

        // PUBLIC 상태의 코스만 필터링
        val courseIds = courseIdCountMap.keys.toList()
        val courses = courseApiRepository.findCoursesWithTagsByIdsAndStatus(courseIds, CourseStatus.PUBLIC)

        // 코스별 PopularCourse 엔티티 생성 및 저장
        courses.forEach { course ->
            val useCount = courseIdCountMap[course.id] ?: 0

            val popularCourse = PopularCourse(
                date = LocalDate.now().minusDays(1),
                courseId = course.id,
                useCount = useCount
            )
            popularCourseRepository.save(popularCourse)
        }

        println("스케줄러 실행 종료: ${LocalDateTime.now()}") // 스케줄러 종료 로그
    }
}