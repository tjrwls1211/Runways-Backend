package syntax.backend.runways.service

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseSegmentMapping
import syntax.backend.runways.repository.CourseSegmentMappingRepository

@Service
class CourseMappingService(
    private val entityManager: EntityManager,
    private val courseSegmentMappingRepository: CourseSegmentMappingRepository
) {

    fun mapSegmentsToCourse(course: Course) {
        val sql = """
            SELECT gid
            FROM walkroads
            WHERE ST_DWithin(coordinate, ST_SetSRID(:line, 4326), 15)
            ORDER BY ST_Distance(coordinate, ST_SetSRID(:line, 4326))
            LIMIT 10
        """.trimIndent()

        val gIDs: List<Int> = entityManager.createNativeQuery(sql)
            .setParameter("line", course.coordinate)
            .resultList
            .map { (it as Number).toInt() }

        val mappings = gIDs.map { gid ->
            CourseSegmentMapping(course = course, segmentGid = gid)
        }

        courseSegmentMappingRepository.saveAll(mappings)
    }
}
