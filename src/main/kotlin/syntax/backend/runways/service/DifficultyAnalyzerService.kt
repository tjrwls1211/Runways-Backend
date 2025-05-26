package syntax.backend.runways.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.CourseDifficulty
import syntax.backend.runways.repository.CourseRepository
import java.util.UUID

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
            val difficulty = determineDifficulty(absSlope)
            val course = courseRepository.findById(courseId).orElseThrow()
            course.difficulty = difficulty
            courseRepository.save(course)
        }
    }

    private fun determineDifficulty(absSlope: Double): CourseDifficulty {
        return when {
            absSlope >= 15 -> CourseDifficulty.HARD
            absSlope >= 7 -> CourseDifficulty.NORMAL
            else -> CourseDifficulty.EASY
        }
    }
}
