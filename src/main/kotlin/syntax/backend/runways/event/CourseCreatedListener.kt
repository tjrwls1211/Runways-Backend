package syntax.backend.runways.event

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.event.TransactionPhase
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.service.CourseMappingService
import syntax.backend.runways.service.DifficultyAnalyzerService

@Component
class CourseCreatedListener(
    private val courseMappingService: CourseMappingService,
    private val difficultyAnalyzerService: DifficultyAnalyzerService,
    private val courseRepository: CourseRepository
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    open fun handleCourseCreated(event: CourseCreatedEvent) {
        try {
            println("AFTER_COMMIT 실행됨: ${event.courseId}, Thread=${Thread.currentThread().name}")
            val course = courseRepository.findById(event.courseId).orElseThrow()
            courseMappingService.mapSegmentsToCourse(course)
            difficultyAnalyzerService.analyzeAndSaveDifficulty(event.courseId)
        } catch (e: Exception) {
            println("후처리 실패: ${e.message}")
            e.printStackTrace()
        }
    }
}
