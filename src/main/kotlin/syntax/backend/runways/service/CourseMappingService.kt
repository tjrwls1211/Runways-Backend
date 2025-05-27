package syntax.backend.runways.service

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseSegmentMapping
import syntax.backend.runways.repository.CourseSegmentMappingRepository

@Service
class CourseMappingService(
    private val entityManager: EntityManager,
    private val courseSegmentMappingRepository: CourseSegmentMappingRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun mapSegmentsToCourse(course: Course) {
        logger.info("코스 세그먼트 매핑 시작: courseId=${course.id}")

        val sql = """
            SELECT gid
            FROM walkroads
            WHERE ST_Intersects(
                      ST_Transform(coordinate, 3857),
                      ST_Buffer(ST_Transform(ST_SetSRID(:line, 4326), 3857), 5)
                  )
              AND ST_Length(
                      ST_Intersection(
                          ST_Transform(coordinate, 3857),
                          ST_Buffer(ST_Transform(ST_SetSRID(:line, 4326), 3857), 5)
                      )
                  ) / ST_Length(ST_Transform(coordinate, 3857)) >= 0.5
            ORDER BY ST_Distance(
                ST_Transform(coordinate, 3857),
                ST_Transform(ST_SetSRID(:line, 4326), 3857)
            )
            LIMIT 100
        """.trimIndent()

        val gIDs: List<Int> = entityManager.createNativeQuery(sql)
            .setParameter("line", course.coordinate)
            .resultList
            .map { (it as Number).toInt() }

        logger.info("쿼리 결과 GID 개수: ${gIDs.size}")

        val existingGids = courseSegmentMappingRepository.findSegmentGidsByCourseId(course.id)
        val newGIDs = gIDs.distinct().filterNot { it in existingGids }

        logger.info("신규 매핑할 GID 개수: ${newGIDs.size}")

        if (newGIDs.isEmpty()) {
            logger.info("신규 GID 없음 - 매핑 작업 종료")
            return
        }

        val mappings = newGIDs.map { gid -> CourseSegmentMapping(course = course, segmentGid = gid) }

        mappings.chunked(50).forEachIndexed { index, chunk ->
            logger.info("청크 ${index + 1} 저장 시작 (size=${chunk.size})")
            courseSegmentMappingRepository.saveAllAndFlush(chunk)
            entityManager.clear()
            logger.info("청크 ${index + 1} 저장 완료")
        }

        logger.info("코스 세그먼트 매핑 완료: courseId=${course.id}")
    }
}
