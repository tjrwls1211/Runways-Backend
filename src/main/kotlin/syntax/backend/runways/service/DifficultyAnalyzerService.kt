package syntax.backend.runways.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseDifficulty
import syntax.backend.runways.repository.CourseRepository
import java.util.UUID
import kotlin.compareTo

@Service
class DifficultyAnalyzerService(
    private val jdbcTemplate: JdbcTemplate,
    private val courseRepository: CourseRepository
) {
    fun analyzeAndSaveDifficulty(courseId: UUID) {
        val sql = """
            WITH base AS (
            SELECT w.incline_avg, w.length_m
            FROM course_segment_mapping csm
            JOIN walkroads w ON w.gid = csm.segment_gid
            WHERE csm.course_id = ?
            )
            SELECT 
            SUM(ABS(base.incline_avg) * base.length_m) / NULLIF(SUM(base.length_m), 0) AS abs_slope
            FROM base
    """.trimIndent()

        val absSlope: Double? = jdbcTemplate.queryForObject(
            sql,
            arrayOf(courseId),
            Double::class.java
        )

        if (absSlope != null) {
            val course = courseRepository.findById(courseId).orElseThrow()
            val difficulty = determineDifficulty(absSlope, course)
            course.difficulty = difficulty
            courseRepository.save(course)
        }
    }

    private fun determineDifficulty(absSlope: Double, course: Course): CourseDifficulty {
        return when {
            // 거리 기준으로 HARD
            course.distance > 5.0 -> {
                when {
                    absSlope >= 5 -> CourseDifficulty.HARD
                    absSlope >= 2 -> CourseDifficulty.NORMAL
                    else -> CourseDifficulty.EASY
                }
            }
            // 거리 기준으로 NORMAL
            course.distance > 3.0 -> {
                when {
                    absSlope >= 6 -> CourseDifficulty.HARD
                    absSlope >= 3 -> CourseDifficulty.NORMAL
                    else -> CourseDifficulty.EASY
                }
            }
            // 거리 기준으로 EASY
            course.distance > 1.0 -> {
                when {
                    absSlope >= 7 -> CourseDifficulty.HARD
                    absSlope >= 4 -> CourseDifficulty.NORMAL
                    else -> CourseDifficulty.EASY
                }
            }
            // 거리 1km 이하
            else -> when {
                absSlope >= 8 -> CourseDifficulty.HARD
                absSlope >= 6 -> CourseDifficulty.NORMAL
                else -> {
                    if (absSlope < 6) CourseDifficulty.EASY else CourseDifficulty.NORMAL
                }
            }
        }
    }
}
